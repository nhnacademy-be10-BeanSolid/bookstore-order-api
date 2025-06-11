package com.nhnacademy.bookstoreorderapi.order.service;


import com.nhnacademy.bookstoreorderapi.order.domain.entity.Order;
import com.nhnacademy.bookstoreorderapi.order.domain.entity.OrderItem;
import com.nhnacademy.bookstoreorderapi.order.domain.entity.OrderStatus;
import com.nhnacademy.bookstoreorderapi.order.domain.entity.Wrapping;
import com.nhnacademy.bookstoreorderapi.order.domain.exception.ResourceNotFoundException;
import com.nhnacademy.bookstoreorderapi.order.dto.OrderItemDto;
import com.nhnacademy.bookstoreorderapi.order.dto.OrderRequestDto;
import com.nhnacademy.bookstoreorderapi.order.dto.OrderResponseDto;
import com.nhnacademy.bookstoreorderapi.order.repository.OrderRepository;
import com.nhnacademy.bookstoreorderapi.order.repository.WrappingRepository;
import com.nhnacademy.bookstoreorderapi.order.repository.CanceledOrderRepository;
import com.nhnacademy.bookstoreorderapi.order.domain.entity.CanceledOrder;        

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final CanceledOrderRepository canceledOrderRepository;
    private final OrderRepository orderRepository;
    private final WrappingRepository wrappingRepository;

    @Transactional
    public OrderResponseDto createOrder(OrderRequestDto req) {
        // 1) 배송 날짜 결정 (희망일 또는 기본 오늘)
        LocalDate effectiveDate = (req.getDeliveryDate() != null) ? req.getDeliveryDate() : LocalDate.now();
        LocalDateTime deliveryAt = effectiveDate.atStartOfDay();

        // 2) 엔티티 생성 (기본 정보 세팅)
        Order order = Order.builder()
                .userId(req.getUserId())
                .guestName(req.getGuestName())
                .guestPhone(req.getGuestPhone())
                .status(OrderStatus.PENDING)
                .requestedAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .deliveryAt(deliveryAt)
                .totalPrice(0)
                .deliveryFee(0)
                .finalPrice(0)
                .build();

        // 3) 아이템별 가격 계산 및 엔티티에 추가
        int sum = 0;
        for (OrderItemDto dto : req.getItems()) {
            Wrapping wrap = null;
            if (dto.getWrappingId() != null) {
                wrap = wrappingRepository.findById(dto.getWrappingId())
                        .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 포장 ID: " + dto.getWrappingId()));
            }

            int unitPrice = 10_000;
            int wrapFee = Boolean.TRUE.equals(dto.getGiftWrapped()) && wrap != null
                    ? wrap.getPrice() * dto.getQuantity()
                    : 0;

            sum += unitPrice * dto.getQuantity() + wrapFee;

            OrderItem item = OrderItem.builder()
                    .bookId(dto.getBookId())
                    .quantity(dto.getQuantity())
                    .giftWrapped(dto.getGiftWrapped())
                    .unitPrice(unitPrice)
                    .wrapping(wrap)
                    .build();

            order.addItem(item);
        }

        // 4) 배송비 및 최종금액 세팅
        int deliveryFee = 3_000;
        order.setTotalPrice(sum);
        order.setDeliveryFee(deliveryFee);
        order.setFinalPrice(sum + deliveryFee);

        // 5) 저장
        Order saved = orderRepository.save(order);

        // 6) 응답 DTO 생성
        String userInfo = saved.getUserId() != null
                ? "회원 ID: " + saved.getUserId()
                : "비회원: " + saved.getGuestName() + " (" + saved.getGuestPhone() + ")";
        String message = String.format("[%s] 주문 생성됨 / 총액: %d원 / 배송비: %d원 / 결제금액: %d원",
                userInfo, saved.getTotalPrice(), saved.getDeliveryFee(), saved.getFinalPrice());

        return OrderResponseDto.builder()
                .orderId(saved.getId())
                .totalPrice(saved.getTotalPrice())
                .deliveryFee(saved.getDeliveryFee())
                .finalPrice(saved.getFinalPrice())
                .message(message)
                .build();
    }

    @Transactional(readOnly = true)
    public List<OrderResponseDto> listAll() {
      
          return orderRepository.findAll().stream()
          .map(o -> {
              String userInfo = o.getUserId() != null
                      ? "회원 ID: " + o.getUserId()
                      : "비회원: " + o.getGuestName() + " (" + o.getGuestPhone() + ")";
              String message = String.format("[%s] 주문 생성됨 / 총액: %d원 / 배송비: %d원 / 결제금액: %d원",
                      userInfo, o.getTotalPrice(), o.getDeliveryFee(), o.getFinalPrice());

              return OrderResponseDto.builder()
                      .orderId(o.getId())
                      .totalPrice(o.getTotalPrice())
                      .deliveryFee(o.getDeliveryFee())
                      .finalPrice(o.getFinalPrice())
                      .message(message)
                      .build();
          })
          .collect(Collectors.toList());
    }
    @Transactional
    public void cancelOrder(Long orderId, String reason) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("주문을 찾을 수 없습니다."));
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException("배송 전 주문만 취소 가능합니다.");
        }

        // 상태 변경
        order.setStatus(OrderStatus.CANCELED);

        // 취소 기록 저장
        CanceledOrder record = CanceledOrder.builder()
                .orderId(orderId)
                .canceledAt(LocalDateTime.now())
                .reason(reason)
                .build();
        canceledOrderRepository.save(record);

        // **이 부분만 추가**: 변경된 주문 상태를 저장해야 테스트가 통과합니다.
        orderRepository.save(order);
    }

    public void changeStatus(Long orderId, OrderStatus newStatus) {

        Order order = orderRepository.findById(orderId).orElseThrow(() -> new ResourceNotFoundException("주문을 찾을 수 없음"));

        OrderStatus oldStatus = order.getStatus();
        if (!oldStatus.canTransitionTo(newStatus)) {
            throw new IllegalStateException(String.format("상태 전이 불가: %s -> %s", oldStatus, newStatus));
        }

        order.setStatus(newStatus);
    }


}
