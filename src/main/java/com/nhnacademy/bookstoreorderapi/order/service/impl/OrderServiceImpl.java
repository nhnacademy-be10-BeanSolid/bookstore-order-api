package com.nhnacademy.bookstoreorderapi.order.service.impl;

import com.nhnacademy.bookstoreorderapi.order.domain.entity.*;
import com.nhnacademy.bookstoreorderapi.order.dto.*;
import com.nhnacademy.bookstoreorderapi.order.domain.exception.BadRequestException;
import com.nhnacademy.bookstoreorderapi.order.domain.exception.InvalidOrderStatusChangeException;
import com.nhnacademy.bookstoreorderapi.order.domain.exception.ResourceNotFoundException;
import com.nhnacademy.bookstoreorderapi.order.repository.*;
import com.nhnacademy.bookstoreorderapi.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final WrappingRepository wrappingRepository;
    private final CanceledOrderRepository canceledOrderRepository;
    private final OrderStatusLogRepository statusLogRepository;
    private final ReturnRepository returnRepository;

    @Override
    @Transactional
    public OrderResponseDto createOrder(OrderRequestDto req) {
        LocalDate effectiveDate = req.getDeliveryDate() != null
                ? req.getDeliveryDate()
                : LocalDate.now();
        LocalDateTime deliveryAt = effectiveDate.atStartOfDay();

        Order order = Order.builder()
                .userId(req.getUserId())
                .guestName(req.getGuestName())
                .guestPhone(req.getGuestPhone())
                .status(OrderStatus.PENDING)
                .orderDate(LocalDate.now())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .requestedDeliveryDate(effectiveDate)
                .totalPrice(0)
                .deliveryFee(0)
                .finalPrice(0)
                .build();

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

            OrderItem item = OrderItem.builder()
                    .bookId(dto.getBookId())
                    .quantity(dto.getQuantity())
                    .isGiftWrapped(dto.getGiftWrapped())
                    .unitPrice(unitPrice)
                    .wrapping(wrap)
                    .build();
            order.addItem(item);
        }

        int deliveryFee = (req.getUserId() != null && sum >= 30_000) ? 0 : 5_000;
        order.setTotalPrice(sum);
        order.setDeliveryFee(deliveryFee);
        order.setFinalPrice(sum + deliveryFee);

        Order saved = orderRepository.save(order);

        String userInfo = saved.getUserId() != null
                ? "회원 ID: " + saved.getUserId()
                : "비회원: " + saved.getGuestName() + " (" + saved.getGuestPhone() + ")";
        String message = String.format("[%s] 주문 생성됨 / 총액: %d원 / 배송비: %d원 / 결제금액: %d원",
                userInfo, saved.getTotalPrice(), saved.getDeliveryFee(), saved.getFinalPrice());

        return OrderResponseDto.builder()
                .orderId(saved.getOrderId())
                .totalPrice(saved.getTotalPrice())
                .deliveryFee(saved.getDeliveryFee())
                .finalPrice(saved.getFinalPrice())
                .message(message)
                .build();
    }

    // ↓ 새로 추가한 회원별 주문 조회 메서드
    @Override
    @Transactional(readOnly = true)
    public List<OrderResponseDto> listByUser(String userId) {
        return orderRepository.findAllByUserId(userId).stream()
                .map(o -> {
                    String userInfo = o.getUserId() != null
                            ? "회원 ID: " + o.getUserId()
                            : "비회원: " + o.getGuestName() + " (" + o.getGuestPhone() + ")";
                    String message = String.format("[%s] 주문 생성됨 / 총액: %d원 / 배송비: %d원 / 결제금액: %d원",
                            userInfo, o.getTotalPrice(), o.getDeliveryFee(), o.getFinalPrice());
                    return OrderResponseDto.builder()
                            .orderId(o.getOrderId())
                            .totalPrice(o.getTotalPrice())
                            .deliveryFee(o.getDeliveryFee())
                            .finalPrice(o.getFinalPrice())
                            .message(message)
                            .build();
                })
                .collect(Collectors.toList());
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
    public StatusChangeResponseDto changeStatus(Long orderId,
                                                OrderStatus newStatus,
                                                Long changedBy,
                                                String memo) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("주문을 찾을 수 없습니다."));
        OrderStatus oldStatus = order.getStatus();
        if (!oldStatus.canTransitionTo(newStatus)) {
            throw new InvalidOrderStatusChangeException(
                    String.format("상태 전이 불가: %s → %s", oldStatus, newStatus));
        }

        OrderStatusLog log = OrderStatusLog.builder()
                .orderId(orderId)
                .oldStatus(oldStatus)
                .newStatus(newStatus)
                .changedAt(LocalDateTime.now())
                .changedBy(changedBy)
                .memo(memo)
                .build();
        statusLogRepository.save(log);

        order.setStatus(newStatus);
        orderRepository.save(order);

        return StatusChangeResponseDto.builder()
                .orderId(orderId)
                .oldStatus(oldStatus)
                .newStatus(newStatus)
                .changedAt(log.getChangedAt())
                .changedBy(changedBy)
                .memo(memo)
                .build();
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
                .map(log -> OrderStatusLogDto.builder()
                        .orderStateId(log.getOrderStateId())
                        .orderId(log.getOrderId())
                        .oldStatus(log.getOldStatus())
                        .newStatus(log.getNewStatus())
                        .changedAt(log.getChangedAt())
                        .changedBy(log.getChangedBy())
                        .memo(log.getMemo())
                        .build())
                .collect(Collectors.toList());
    }
}