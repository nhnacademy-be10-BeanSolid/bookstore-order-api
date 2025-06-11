package com.nhnacademy.bookstoreorderapi.order.controller;


import com.nhnacademy.bookstoreorderapi.order.dto.*;
import com.nhnacademy.bookstoreorderapi.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/orders")
public class OrderController {
    private final OrderService orderService;

    @GetMapping
    public List<OrderResponseDto> listAllJson() {
        return orderService.listAll();
    }

    @PostMapping
    public ResponseEntity<ResponseDto> createOrder(@Valid @RequestBody OrderRequestDto orderForm) {
        try {
            validateOrderType(orderForm);
            OrderResponseDto created = orderService.createOrder(orderForm);
            return ResponseEntity.ok(created);
        } catch (IllegalArgumentException e) {
            ErrorResponseDto error = new ErrorResponseDto(e.getMessage());
            return ResponseEntity.badRequest()
                                 .body(error);
        }
    }

    @PatchMapping(path = "/{orderId}/status")
    public ResponseEntity<Void> changeOrderStatus(
            @PathVariable Long orderId,
            @RequestBody StatusChangeDto dto) {

        orderService.changeStatus(orderId, dto.getNewStatus());
        return ResponseEntity.ok().build();
    }

    private void validateOrderType(OrderRequestDto req) {
        if ("guest".equalsIgnoreCase(req.getOrderType())) {
            if (req.getGuestName() == null || req.getGuestPhone() == null) {
                throw new IllegalArgumentException("비회원 주문은 이름과 전화번호가 필요합니다.");
            }
        } else if ("member".equalsIgnoreCase(req.getOrderType())) {
            if (req.getUserId() == null) {
                throw new IllegalArgumentException("회원 주문은 userId가 필요합니다.");
            }
        } else {
            throw new IllegalArgumentException("orderType은 'member' 또는 'guest'여야 합니다.");
        }
    }
    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<?> cancelOrder(
            @PathVariable Long orderId,
            @RequestBody(required = false) CancelOrderRequestDto dto
    ) {
        String reason = (dto != null ? dto.getReason() : null);
        try {
            orderService.cancelOrder(orderId, reason);
            return ResponseEntity.ok().build();
        } catch (IllegalStateException | ResourceNotFoundException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}