package com.nhnacademy.bookstoreorderapi.order.service.impl;

import com.nhnacademy.bookstoreorderapi.order.domain.entity.*;
import com.nhnacademy.bookstoreorderapi.order.domain.exception.BadRequestException;
import com.nhnacademy.bookstoreorderapi.order.domain.exception.InvalidOrderStatusChangeException;
import com.nhnacademy.bookstoreorderapi.order.domain.exception.ResourceNotFoundException;
import com.nhnacademy.bookstoreorderapi.order.dto.*;
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

    private final OrderRepository          orderRepository;
    private final WrappingRepository       wrappingRepository;
    private final CanceledOrderRepository  canceledOrderRepository;
    private final OrderStatusLogRepository statusLogRepository;

    /*───────────────────────────────────────────────────────
     * 1. 주문 생성
     *──────────────────────────────────────────────────────*/
    @Override
    @Transactional
    public OrderResponseDto createOrder(OrderRequestDto req) {

        /* ① 배송 요청일(없으면 오늘) */
        LocalDate deliveryDate = req.getDeliveryDate() != null
                ? req.getDeliveryDate()
                : LocalDate.now();

        /* ② 주문 엔티티 구성 */
        Order order = Order.builder()
                .userId(req.getUserId())          // 회원 ID (member)
                .guestId(req.getGuestId())        // 비회원 ID (guest)
                .orderAddress(req.getOrderAddress())
                .status(OrderStatus.PENDING)
                .orderDate(LocalDate.now())
                .requestedDeliveryDate(deliveryDate)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .totalPrice(0)                    // ↓ 아래에서 계산
                .deliveryFee(0)
                .build();

        /* ③ 주문-상품( OrderItem ) 처리 */
        int total = 0;
        for (OrderItemDto dto : req.getItems()) {

            Wrapping wrapping = null;
            if (dto.getWrappingId() != null) {
                wrapping = wrappingRepository.findById(dto.getWrappingId())
                        .orElseThrow(() -> new BadRequestException(
                                "잘못된 wrappingId : " + dto.getWrappingId()));
            }

            int unitPrice = 10_000;                           // 예시용 기본 단가
            int wrapFee   = Boolean.TRUE.equals(dto.getGiftWrapped()) && wrapping != null
                    ? wrapping.getPrice() * dto.getQuantity()
                    : 0;

            total += unitPrice * dto.getQuantity() + wrapFee;

            order.addItem(OrderItem.builder()
                    .bookId(dto.getBookId())
                    .quantity(dto.getQuantity())
                    .unitPrice(unitPrice)
                    .isGiftWrapped(dto.getGiftWrapped())
                    .wrapping(wrapping)
                    .build());
        }

        /* ④ 배송비 계산(예: 회원 3만↑ 무료, 그 외 5천) */
        int deliveryFee = (order.getUserId() != null && total >= 30_000)
                ? 0 : Order.DEFAULT_DELIVERY_FEE;

        order.setTotalPrice(total);
        order.setDeliveryFee(deliveryFee);

        Order saved = orderRepository.save(order);

        /* ⑤ 응답 변환 */
        return OrderResponseDto.createFrom(saved);
    }

    /*───────────────────────────────────────────────────────
     * 2. 회원별 주문 목록
     *──────────────────────────────────────────────────────*/
    @Override
    @Transactional(readOnly = true)
    public List<OrderResponseDto> listByUser(String userId) {
        return orderRepository.findAllByUserId(userId).stream()
                .map(OrderResponseDto::createFrom)
                .collect(Collectors.toList());
    }

    /*───────────────────────────────────────────────────────
     * 3. 주문 취소
     *──────────────────────────────────────────────────────*/
    @Override
    @Transactional
    public void cancelOrder(Long orderId, String reason) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("주문을 찾을 수 없습니다."));

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

        orderRepository.save(order);
    }

    /*───────────────────────────────────────────────────────
     * 4. 주문 상태 변경
     *──────────────────────────────────────────────────────*/
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
                    String.format("상태 전이 불가 : %s → %s", oldStatus, newStatus));
        }

        OrderStatusLog log = statusLogRepository.save(
                OrderStatusLog.builder()
                        .orderId(orderId)
                        .oldStatus(oldStatus)
                        .newStatus(newStatus)
                        .changedAt(LocalDateTime.now())
                        .changedBy(changedBy)
                        .memo(memo)
                        .build());

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

    /*───────────────────────────────────────────────────────
     * 5. 반품 요청
     *──────────────────────────────────────────────────────*/
    @Override
    @Transactional
    public int requestReturn(Long orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("주문을 찾을 수 없습니다."));

        if (order.getStatus() == OrderStatus.RETURNED) {
            throw new InvalidOrderStatusChangeException("이미 반품 처리된 주문입니다.");
        }

        order.setStatus(OrderStatus.RETURNED);
        orderRepository.save(order);

        /* 예시 : 반품 수수료 2,500원 제외 */
        return order.getTotalPrice() - 2_500;
    }

    /*───────────────────────────────────────────────────────
     * 6. 상태 변경 이력 조회
     *──────────────────────────────────────────────────────*/
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