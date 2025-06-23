// src/main/java/com/nhnacademy/bookstoreorderapi/order/service/impl/OrderServiceImpl.java
package com.nhnacademy.bookstoreorderapi.order.service.impl;

import com.nhnacademy.bookstoreorderapi.order.domain.entity.*;
import com.nhnacademy.bookstoreorderapi.order.domain.exception.*;
import com.nhnacademy.bookstoreorderapi.order.dto.*;
import com.nhnacademy.bookstoreorderapi.order.repository.*;
import com.nhnacademy.bookstoreorderapi.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final WrappingRepository wrappingRepository;
    private final CanceledOrderRepository canceledOrderRepository;
    private final OrderStatusLogRepository statusLogRepository;
    private final TaskScheduler taskScheduler;
    private final ReturnsRepository returnRepository;

    private static final Duration DELIVERY_DELAY = Duration.ofSeconds(5);

    @Override
    @Transactional
    public long createOrder(OrderRequestDto req) {
        Order order = Order.createFrom(req);
        Order saved = orderRepository.save(order);
        return saved.getId();  // DB PK 리턴
    }

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

    @Override
    @Transactional
    public void cancelOrder(long orderId, String reason) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("주문이 없습니다: " + orderId));

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
        // 상태 변경 저장
        orderRepository.save(order);
    }

    @Override
    @Transactional
    public StatusChangeResponseDto changeStatus(
            long orderId,
            OrderStatus newStatus,
            long changedBy,
            String memo) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("주문이 없습니다: " + orderId));

        OrderStatus old = order.getStatus();
        if (!old.canTransitionTo(newStatus)) {
            throw new InvalidOrderStatusChangeException(
                    String.format("상태 전이 불가: %s → %s", old, newStatus));
        }

        OrderStatusLog log = OrderStatusLog.createFrom(
                orderId, old, newStatus, changedBy, memo);
        statusLogRepository.save(log);

        order.setStatus(newStatus);
        orderRepository.save(order);

        if (newStatus == OrderStatus.SHIPPING) {
            taskScheduler.schedule(
                    () -> completeDelivery(orderId),
                    Instant.now().plus(DELIVERY_DELAY)
            );
        }

        return StatusChangeResponseDto.createFrom(log);
    }

    /** 배송 자동 완료 **/
    @Transactional
    protected void completeDelivery(long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("주문이 없습니다: " + orderId));
        if (order.getStatus() != OrderStatus.SHIPPING) {
            return;
        }

        OrderStatusLog log = OrderStatusLog.createFrom(
                orderId, OrderStatus.SHIPPING, OrderStatus.COMPLETED, 0L, "자동완료");
        statusLogRepository.save(log);

        order.setStatus(OrderStatus.COMPLETED);
        orderRepository.save(order);
    }

    @Override
    @Transactional
    public int requestReturn(long orderId, ReturnRequestDto dto) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("주문이 없습니다: " + orderId));

        if (order.getStatus() == OrderStatus.RETURNED) {
            throw new InvalidOrderStatusChangeException("이미 반품 처리된 주문입니다.");
        }

        order.setStatus(OrderStatus.RETURNED);
        orderRepository.save(order);

        OrderReturn ret = OrderReturn.createFrom(order, dto);
        returnRepository.save(ret);

        return order.getTotalPrice() - OrderReturn.RETURNS_FEE;
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderStatusLogDto> getStatusLog(long orderId) {
        // 상태 로그는 FK(order.id) 로 조회
        return statusLogRepository.findByOrderId(orderId).stream()
                .map(OrderStatusLogDto::createFrom)
                .collect(Collectors.toList());
    }
}