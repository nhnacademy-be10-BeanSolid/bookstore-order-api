// src/main/java/com/nhnacademy/bookstoreorderapi/order/service/impl/OrderServiceImpl.java
package com.nhnacademy.bookstoreorderapi.order.service.impl;

import com.nhnacademy.bookstoreorderapi.order.domain.entity.CanceledOrder;
import com.nhnacademy.bookstoreorderapi.order.domain.entity.Order;
import com.nhnacademy.bookstoreorderapi.order.domain.entity.OrderItem;
import com.nhnacademy.bookstoreorderapi.order.domain.entity.OrderReturn;
import com.nhnacademy.bookstoreorderapi.order.domain.entity.OrderStatus;
import com.nhnacademy.bookstoreorderapi.order.domain.entity.OrderStatusLog;
import com.nhnacademy.bookstoreorderapi.order.domain.exception.InvalidOrderStatusChangeException;
import com.nhnacademy.bookstoreorderapi.order.domain.exception.OrderNotFoundException;
import com.nhnacademy.bookstoreorderapi.order.domain.exception.ResourceNotFoundException;
import com.nhnacademy.bookstoreorderapi.order.dto.OrderItemDto;
import com.nhnacademy.bookstoreorderapi.order.dto.OrderRequestDto;
import com.nhnacademy.bookstoreorderapi.order.dto.OrderResponseDto;
import com.nhnacademy.bookstoreorderapi.order.dto.ReturnRequestDto;
import com.nhnacademy.bookstoreorderapi.order.dto.StatusChangeResponseDto;
import com.nhnacademy.bookstoreorderapi.order.repository.CanceledOrderRepository;
import com.nhnacademy.bookstoreorderapi.order.repository.OrderRepository;
import com.nhnacademy.bookstoreorderapi.order.repository.OrderStatusLogRepository;
import com.nhnacademy.bookstoreorderapi.order.repository.ReturnsRepository;
import com.nhnacademy.bookstoreorderapi.order.repository.WrappingRepository;
import com.nhnacademy.bookstoreorderapi.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository           orderRepository;
    private final WrappingRepository        wrappingRepository;
    private final CanceledOrderRepository   canceledOrderRepository;
    private final OrderStatusLogRepository  statusLogRepository;
    private final ReturnsRepository         returnRepository;
    private final TaskScheduler             taskScheduler;

    /* ───── 비즈니스 상수 ───── */
    private static final int FREE_DELIVERY_THRESHOLD = 30_000;   // 회원 3만원 이상 무료
    private static final int DEFAULT_DELIVERY_FEE    = 5_000;    // 그 외 5천원
    private static final Duration DELIVERY_DELAY     = Duration.ofSeconds(5);

    /* ───────────────────────── 주문 생성 ───────────────────────── */
    @Override
    @Transactional
    public long createOrder(OrderRequestDto req) {

        /* 1) Order 기본 정보 */
        Order order = Order.createFrom(req);

        /* 2) DTO → OrderItem (단가를 JSON 에서 그대로 사용) */
        for (OrderItemDto dto : req.getItems()) {
            if (dto.getUnitPrice() == null) {
                throw new IllegalArgumentException(
                        "unitPrice(단가)가 누락되었습니다: bookId=" + dto.getBookId());
            }

            var wrapping = Boolean.TRUE.equals(dto.getGiftWrapped())
                    ? wrappingRepository.findById(dto.getWrappingId())
                    .orElseThrow(() ->
                            new ResourceNotFoundException("포장 옵션이 없습니다: " + dto.getWrappingId()))
                    : null;

            OrderItem item = OrderItem.createFrom(dto, wrapping, dto.getUnitPrice());
            order.addItem(item);
        }

        /* 3) 총 상품 금액 */
        int total = order.getItems().stream()
                .mapToInt(i -> i.getUnitPrice() * i.getQuantity())
                .sum();
        order.setTotalPrice(total);

        /* 4) 배송비 */
        boolean isMember   = "member".equalsIgnoreCase(req.getOrderType());
        int     deliveryFee = (isMember && total >= FREE_DELIVERY_THRESHOLD)
                ? 0
                : DEFAULT_DELIVERY_FEE;
        order.setDeliveryFee(deliveryFee);

        /* 5) 저장 후 PK 확보 (orders, order_item만 INSERT) */
        Order savedOrder = orderRepository.save(order);

        return savedOrder.getId();
    }

    /* ───────────────────────── 주문 조회 (사용자별) ───────────────────────── */
    @Override
    @Transactional(readOnly = true)
    public List<OrderResponseDto> listByUser(String userId) {
        var orders = orderRepository.findByUserId(userId);
        if (orders.isEmpty()) {
            throw new OrderNotFoundException("주문을 찾을 수 없습니다: " + userId);
        }
        return orders.stream()
                .map(OrderResponseDto::createFrom)
                .collect(Collectors.toList());
    }

    /* ───────────────────────── 주문 취소 ───────────────────────── */
    @Override
    @Transactional
    public void cancelOrder(long orderId, String reason) {
        var order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("주문이 없습니다: " + orderId));

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new InvalidOrderStatusChangeException("배송 전(PENDING) 상태만 취소 가능합니다.");
        }
        order.setStatus(OrderStatus.CANCELED);
        canceledOrderRepository.save(
                CanceledOrder.builder()
                        .orderId(orderId)
                        .canceledAt(LocalDateTime.now())
                        .reason(reason)
                        .build()
        );
    }

    /* ───────────────────────── 주문 상태 변경 ───────────────────────── */
    @Override
    @Transactional
    public StatusChangeResponseDto changeStatus(long orderId,
                                                OrderStatus newStatus,
                                                long changedBy,
                                                String memo) {
        var order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("주문이 없습니다: " + orderId));

        var oldStatus = order.getStatus();
        if (!oldStatus.canTransitionTo(newStatus)) {
            throw new InvalidOrderStatusChangeException(
                    String.format("상태 전이 불가: %s → %s", oldStatus, newStatus));
        }

        // 로그 저장
        var log = OrderStatusLog.createFrom(orderId, oldStatus, newStatus, changedBy, memo);
        statusLogRepository.save(log);

        order.setStatus(newStatus);

        // 배송 중 → 배송 완료 자동 전환 예약
        if (newStatus == OrderStatus.SHIPPING) {
            taskScheduler.schedule(
                    () -> completeDelivery(orderId),
                    Instant.now().plus(DELIVERY_DELAY)
            );
        }

        return StatusChangeResponseDto.createFrom(log);
    }

    @Transactional
    protected void completeDelivery(long orderId) {
        var order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("주문이 없습니다: " + orderId));
        if (order.getStatus() != OrderStatus.SHIPPING) return;

        var log = OrderStatusLog.createFrom(
                orderId, OrderStatus.SHIPPING, OrderStatus.COMPLETED,
                0L, "자동완료"
        );
        statusLogRepository.save(log);
        order.setStatus(OrderStatus.COMPLETED);
    }

    /* ───────────────────────── 반품 요청 ───────────────────────── */
    @Override
    @Transactional
    public int requestReturn(long orderId, ReturnRequestDto dto) {
        var order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("주문이 없습니다: " + orderId));

        if (order.getStatus() == OrderStatus.RETURNED) {
            throw new InvalidOrderStatusChangeException("이미 반품 처리된 주문입니다.");
        }
        order.setStatus(OrderStatus.RETURNED);

        var ret = OrderReturn.createFrom(order, dto);
        returnRepository.save(ret);

        // 예시: 상품 금액 – 반품 수수료
        return order.getTotalPrice() - OrderReturn.RETURNS_FEE;
    }

    /* ───────────────────────── 상태 변경 이력 조회 ───────────────────────── */
    @Override
    @Transactional(readOnly = true)
    public List<com.nhnacademy.bookstoreorderapi.order.dto.OrderStatusLogDto> getStatusLog(long orderId) {
        return statusLogRepository.findByOrderId(orderId).stream()
                .map(com.nhnacademy.bookstoreorderapi.order.dto.OrderStatusLogDto::createFrom)
                .collect(Collectors.toList());
    }
}