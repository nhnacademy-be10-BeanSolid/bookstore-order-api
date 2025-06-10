package com.nhnacademy.bookstoreorderapi.order.service;

import com.nhnacademy.bookstoreorderapi.order.domain.entity.Order;
import com.nhnacademy.bookstoreorderapi.order.domain.entity.OrderStatus;
import com.nhnacademy.bookstoreorderapi.order.domain.exception.ResourceNotFoundException;
import com.nhnacademy.bookstoreorderapi.order.dto.OrderItemDto;
import com.nhnacademy.bookstoreorderapi.order.dto.OrderRequestDto;
import com.nhnacademy.bookstoreorderapi.order.dto.OrderResponseDto;
import com.nhnacademy.bookstoreorderapi.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
@Service
@RequiredArgsConstructor
public class OrderService {

    private final Map<Long, OrderResponseDto> orderStore = new HashMap<>();
    private final AtomicLong idGen = new AtomicLong(1);
    private final OrderRepository orderRepository;

    public OrderResponseDto createOrder(OrderRequestDto req) {
        int sum = 0;
        for (OrderItemDto itemDto : req.getItems()) {
            int itemPrice = 10_000 * itemDto.getQuantity();
            int wrapFee = Boolean.TRUE.equals(itemDto.getGiftWrapped()) ? 2_000 * itemDto.getQuantity() : 0;
            sum += itemPrice + wrapFee;
        }

        int deliveryFee = 3_000;
        int finalPrice = sum + deliveryFee;
        long orderId = idGen.getAndIncrement();

        String userInfo = req.getUserId() != null
                ? "회원 ID: " + req.getUserId()
                : "비회원: " + req.getGuestName() + " (" + req.getGuestPhone() + ")";

        String message = String.format("[%s] 주문 생성됨 / 총액: %d원 / 배송비: %d원 / 결제금액: %d원",
                userInfo, sum, deliveryFee, finalPrice);

        OrderResponseDto response = OrderResponseDto.builder()
                .orderId(orderId)
                .totalPrice(sum)
                .deliveryFee(deliveryFee)
                .finalPrice(finalPrice)
                .message(message)
                .build();

        orderStore.put(orderId, response);
        return response;
    }

    public List<OrderResponseDto> listAll() {
        return new ArrayList<>(orderStore.values());
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