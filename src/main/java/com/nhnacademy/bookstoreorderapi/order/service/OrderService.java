package com.nhnacademy.bookstoreorderapi.order.service;

import com.nhnacademy.bookstoreorderapi.order.domain.entity.*;
import com.nhnacademy.bookstoreorderapi.order.domain.exception.InvalidOrderStatusChangeException;
import com.nhnacademy.bookstoreorderapi.order.domain.exception.OrderNotFoundException;
import com.nhnacademy.bookstoreorderapi.order.domain.exception.ResourceNotFoundException;
import com.nhnacademy.bookstoreorderapi.order.dto.*;
import com.nhnacademy.bookstoreorderapi.order.repository.CanceledOrderRepository;
import com.nhnacademy.bookstoreorderapi.order.repository.OrderRepository;
import com.nhnacademy.bookstoreorderapi.order.repository.OrderStatusLogRepository;
import com.nhnacademy.bookstoreorderapi.order.repository.WrappingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final WrappingRepository wrappingRepository;
    private final CanceledOrderRepository canceledOrderRepository;
    private final OrderStatusLogRepository statusLogRepository;

    @Transactional
    public OrderResponseDto createOrder(OrderRequestDto req) {

        Order order = Order.createFrom(req);

        int sum = 0;
        for (OrderItemDto dto : req.getItems()) {
            Wrapping wrap = dto.getWrappingId() != null
                    ? wrappingRepository.findById(dto.getWrappingId())
                    .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 포장 ID: " + dto.getWrappingId()))
                    : null;

            int unitPrice = 10_000;
            int wrapFee = Boolean.TRUE.equals(dto.getGiftWrapped()) && wrap != null
                    ? wrap.getPrice() * dto.getQuantity()
                    : 0;

            sum += unitPrice * dto.getQuantity() + wrapFee;

            OrderItem item = OrderItem.createFrom(dto, wrap, unitPrice);
            order.addItem(item);
        }

        order.setTotalPrice(sum);
        order.setFinalPrice(sum + Order.DELIVERY_FEE);

        Order saved = orderRepository.save(order);

        return OrderResponseDto.createFrom(saved);
    }

    @Transactional(readOnly = true)
    public List<OrderResponseDto> listAll() {
        return orderRepository.findAll().stream()
                .map(OrderResponseDto::createFrom)
                .collect(Collectors.toList());
    }

    @Transactional
    public void cancelOrder(Long orderId, String reason) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("주문을 찾을 수 없습니다."));
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException("배송 전 주문만 취소 가능합니다.");
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
            throw new IllegalStateException(
                    String.format("상태 전이 불가: %s -> %s", oldStatus, newStatus)
            );
        }

        OrderStatusLog log = OrderStatusLog.createFrom(orderId, oldStatus, newStatus, changedBy, memo);
        statusLogRepository.save(log);

        order.setStatus(newStatus);
        orderRepository.save(order);

        return StatusChangeResponseDto.createFrom(log);
    }

    public int requestReturn(Long orderId) {

        Order order = orderRepository.findById(orderId).orElseThrow(() -> new OrderNotFoundException("주문을 찾을 수 없습니다."));
        if (order.getStatus().equals(OrderStatus.RETURNED)) {
            throw new InvalidOrderStatusChangeException("이미 반품된 상품입니다.");
        }

        order.setStatus(OrderStatus.RETURNED);
        orderRepository.save(order);
        return order.getTotalPrice() - 2_500;
    }
  
    @Transactional(readOnly = true)
    public List<OrderStatusLogDto> getStatusLog(Long orderId) {
        // 주문 존재 여부 확인(Optional)
        if (!orderRepository.existsById(orderId)) {
            throw new ResourceNotFoundException("주문을 찾을 수 없습니다.");
        }

        // 로그 조회 후 DTO 변환
        return statusLogRepository.findByOrderId(orderId).stream()
                .map(OrderStatusLogDto::createFrom)
                .collect(Collectors.toList());
    }

    public OrderResponseDto getOrderById(Long orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("주문을 찾을 수 없습니다."));

        return OrderResponseDto.createFrom(order);
    }
}
