package com.nhnacademy.bookstoreorderapi.order.service.impl;

import com.nhnacademy.bookstoreorderapi.order.domain.entity.*;
import com.nhnacademy.bookstoreorderapi.order.domain.exception.BadRequestException;
import com.nhnacademy.bookstoreorderapi.order.domain.exception.InvalidOrderStatusChangeException;
import com.nhnacademy.bookstoreorderapi.order.domain.exception.OrderNotFoundException;
import com.nhnacademy.bookstoreorderapi.order.domain.exception.ResourceNotFoundException;
import com.nhnacademy.bookstoreorderapi.order.dto.*;
import com.nhnacademy.bookstoreorderapi.order.repository.*;
import com.nhnacademy.bookstoreorderapi.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
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
    public OrderResponseDto createOrder(OrderRequestDto req) {

        Order order = Order.createFrom(req);

        int sum = 0;
        for (OrderItemDto dto : req.getItems()) {
            Wrapping wrap = null;
            if (dto.getWrappingId() != null) {
                wrap = wrappingRepository.findById(dto.getWrappingId())
                        .orElseThrow(() -> new BadRequestException(
                                "유효하지 않은 포장 ID: " + dto.getWrappingId()));
            }

            int unitPrice = 10_000;
            int wrapFee = Boolean.TRUE.equals(dto.getGiftWrapped()) && wrap != null
                    ? wrap.getPrice() * dto.getQuantity()
                    : 0;

            sum += unitPrice * dto.getQuantity() + wrapFee;

            OrderItem item = OrderItem.createFrom(dto, wrap, unitPrice);
            order.addItem(item);
        }

        int deliveryFee = (req.getUserId() != null && sum >= 30_000) ? 0 : Order.DEFAULT_DELIVERY_FEE;
        order.setTotalPrice(sum);
        order.setDeliveryFee(deliveryFee);
        order.setFinalPrice(sum + deliveryFee);

        Order saved = orderRepository.save(order);

        return OrderResponseDto.createFrom(saved);
    }

    @Override
    public List<OrderResponseDto> listByUser(String userId) {

        List<Order> orders = orderRepository.findByUserId(userId);
        if (orders.isEmpty()) {
            throw new OrderNotFoundException("주문을 찾을 수 없습니다.");
        }

        return orders.stream()
                .map(OrderResponseDto::createFrom)
                .toList();
    }

    @Override
    @Transactional
    public void cancelOrder(Long orderId, String reason) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("주문을 찾을 수 없습니다."));
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new InvalidOrderStatusChangeException("배송 전 주문만 취소 가능합니다.");
        }
        order.setStatus(OrderStatus.CANCELED);

        CanceledOrder record = CanceledOrder.builder()
                .orderId(orderId)
                .canceledAt(LocalDateTime.now())
                .reason(reason)
                .build();
        canceledOrderRepository.save(record);
        orderRepository.save(order);
    }

    @Override
    @Transactional
    public StatusChangeResponseDto changeStatus(
            Long orderId,
            OrderStatus newStatus,
            Long changedBy,
            String memo
    ) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("주문을 찾을 수 없습니다."));
        OrderStatus oldStatus = order.getStatus();
        if (!oldStatus.canTransitionTo(newStatus)) {
            throw new InvalidOrderStatusChangeException(
                    String.format("상태 전이 불가: %s → %s", oldStatus, newStatus));
        }

        OrderStatusLog log = OrderStatusLog.createFrom(orderId, oldStatus, newStatus, changedBy, memo);
        statusLogRepository.save(log);

        order.setStatus(newStatus);
        orderRepository.save(order);

        if (newStatus == OrderStatus.SHIPPING) {
            scheduleAutoDeliveryComplete(orderId);
        }

        return StatusChangeResponseDto.createFrom(log);
    }

    private void scheduleAutoDeliveryComplete(Long orderId) {

        LocalDateTime runAt = LocalDateTime.now().plus(DELIVERY_DELAY);
        Date triggerTime = Date.from(runAt.atZone(ZoneId.systemDefault()).toInstant());

        taskScheduler.schedule(() -> {
            try {
                completeDelivery(orderId);
            } catch (Exception e) {
                log.error("자동 배송완료 처리 실패 for order {}", orderId, e);
            }
        }, triggerTime);
    }

    @Transactional
    public void completeDelivery(Long orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("주문을 찾을 수 없습니다."));

        if (order.getStatus() != OrderStatus.SHIPPING) {
            return;
        }

        statusLogRepository.save(OrderStatusLog.createFrom(orderId, OrderStatus.SHIPPING, OrderStatus.COMPLETED, 99L, "배송 자동 완료"));
        order.setStatus(OrderStatus.COMPLETED);
        orderRepository.save(order);
    }

    @Override
    @Transactional
    public int requestReturn(Long orderId, ReturnRequestDto dto) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("주문을 찾을 수 없습니다."));
        if (order.getStatus() == OrderStatus.RETURNED) {
            throw new InvalidOrderStatusChangeException("이미 반품된 상품입니다.");
        }
        order.setStatus(OrderStatus.RETURNED);
        orderRepository.save(order);

        Returns returns = Returns.createFrom(order, dto);
        returnRepository.save(returns);

        return order.getTotalPrice() - Returns.RETURNS_FEE;
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderStatusLogDto> getStatusLog(Long orderId) {
        if (!orderRepository.existsById(orderId)) {
            throw new ResourceNotFoundException("주문을 찾을 수 없습니다.");
        }
        return statusLogRepository.findByOrderId(orderId).stream()
                .map(OrderStatusLogDto::createFrom)
                .collect(Collectors.toList());
    }
}

