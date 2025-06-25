// src/main/java/com/nhnacademy/bookstoreorderapi/order/service/impl/OrderServiceImpl.java
package com.nhnacademy.bookstoreorderapi.order.service.impl;

import com.nhnacademy.bookstoreorderapi.order.domain.entity.*;
import com.nhnacademy.bookstoreorderapi.order.domain.exception.*;
import com.nhnacademy.bookstoreorderapi.order.dto.*;
import com.nhnacademy.bookstoreorderapi.order.repository.*;

import com.nhnacademy.bookstoreorderapi.payment.domain.PayType;
import com.nhnacademy.bookstoreorderapi.payment.domain.entity.Payment;
import com.nhnacademy.bookstoreorderapi.payment.repository.PaymentRepository;

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
    private final PaymentRepository         paymentRepository;

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

            Wrapping wrapping = null;
            if (Boolean.TRUE.equals(dto.getGiftWrapped())) {
                wrapping = wrappingRepository.findById(dto.getWrappingId())
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "포장 옵션이 없습니다: " + dto.getWrappingId()));
            }

            OrderItem item = OrderItem.createFrom(dto, wrapping, dto.getUnitPrice());
            order.addItem(item);
        }

        /* 3) 총 상품 금액 */
        int total = order.getItems()
                .stream()
                .mapToInt(i -> i.getUnitPrice() * i.getQuantity())
                .sum();
        order.setTotalPrice(total);

        /* 4) 배송비 */
        boolean isMember   = "member".equalsIgnoreCase(req.getOrderType());
        int     deliveryFee = (isMember && total >= FREE_DELIVERY_THRESHOLD)
                ? 0
                : DEFAULT_DELIVERY_FEE;
        order.setDeliveryFee(deliveryFee);

        /* 5) 저장 후 PK 확보 */
        Order savedOrder = orderRepository.save(order);

        /* 6) 결제 레코드 */
        Payment payment = Payment.builder()
                .orderId(savedOrder.getOrderId())        // varchar(64)
                .payAmount((long) (total + deliveryFee)) // JSON 금액 그대로
                .payType(PayType.valueOf(req.getPayMethod()))
                .payName(req.getOrderName())
                .paySuccessYn(Boolean.TRUE)
                .build();
        paymentRepository.save(payment);

        return savedOrder.getId();
    }

    /* ───────────────────────── 주문 조회 (사용자별) ───────────────────────── */
    @Override
    @Transactional(readOnly = true)
    public List<OrderResponseDto> listByUser(String userId) {

        List<Order> orders = orderRepository.findByUserId(userId);

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

        Order order = orderRepository.findById(orderId)
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
                        .build());
    }

    /* ───────────────────────── 주문 상태 변경 ───────────────────────── */
    @Override
    @Transactional
    public StatusChangeResponseDto changeStatus(long orderId,
                                                OrderStatus newStatus,
                                                long changedBy,
                                                String memo) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("주문이 없습니다: " + orderId));

        OrderStatus oldStatus = order.getStatus();
        if (!oldStatus.canTransitionTo(newStatus)) {
            throw new InvalidOrderStatusChangeException(
                    String.format("상태 전이 불가: %s → %s", oldStatus, newStatus));
        }

        /* 로그 저장 */
        OrderStatusLog log = OrderStatusLog.createFrom(
                orderId, oldStatus, newStatus, changedBy, memo);
        statusLogRepository.save(log);

        /* 실제 상태 변경 */
        order.setStatus(newStatus);

        /* 배송 중 → 배송 완료 자동 전환 예약 */
        if (newStatus == OrderStatus.SHIPPING) {
            taskScheduler.schedule(
                    () -> completeDelivery(orderId),
                    Instant.now().plus(DELIVERY_DELAY)
            );
        }

        return StatusChangeResponseDto.createFrom(log);
    }

    /** 배송 자동 완료 (예약 작업) */
    @Transactional
    protected void completeDelivery(long orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("주문이 없습니다: " + orderId));

        if (order.getStatus() != OrderStatus.SHIPPING) {
            return;
        }

        OrderStatusLog log = OrderStatusLog.createFrom(
                orderId, OrderStatus.SHIPPING, OrderStatus.COMPLETED,
                0L, "자동완료");
        statusLogRepository.save(log);

        order.setStatus(OrderStatus.COMPLETED);
    }

    /* ───────────────────────── 반품 요청 ───────────────────────── */
    @Override
    @Transactional
    public int requestReturn(long orderId, ReturnRequestDto dto) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("주문이 없습니다: " + orderId));

        if (order.getStatus() == OrderStatus.RETURNED) {
            throw new InvalidOrderStatusChangeException("이미 반품 처리된 주문입니다.");
        }

        order.setStatus(OrderStatus.RETURNED);

        OrderReturn ret = OrderReturn.createFrom(order, dto);
        returnRepository.save(ret);

        /* 예시: 상품 금액 – 반품 수수료 */
        return order.getTotalPrice() - OrderReturn.RETURNS_FEE;
    }

    /* ───────────────────────── 상태 변경 이력 조회 ───────────────────────── */
    @Override
    @Transactional(readOnly = true)
    public List<OrderStatusLogDto> getStatusLog(long orderId) {

        return statusLogRepository.findByOrderId(orderId)
                .stream()
                .map(OrderStatusLogDto::createFrom)
                .collect(Collectors.toList());
    }
}