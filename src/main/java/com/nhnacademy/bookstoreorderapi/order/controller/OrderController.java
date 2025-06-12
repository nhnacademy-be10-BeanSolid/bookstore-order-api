package com.nhnacademy.bookstoreorderapi.order.controller;

import com.nhnacademy.bookstoreorderapi.order.dto.*;
import com.nhnacademy.bookstoreorderapi.order.domain.exception.ResourceNotFoundException;
import com.nhnacademy.bookstoreorderapi.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import com.nhnacademy.bookstoreorderapi.order.dto.OrderStatusLogDto;
import com.nhnacademy.bookstoreorderapi.order.dto.StatusChangeResponseDto;
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
            return ResponseEntity.badRequest()
                    .body(new ErrorResponseDto(e.getMessage()));
        }
    }

    @PatchMapping("/{orderId}/status")
    public ResponseEntity<ResponseDto> changeOrderStatus(
            @PathVariable Long orderId,
            @RequestBody @Valid StatusChangeResponseDto dto) {
        try {
            StatusChangeResponseDto resp = orderService.changeStatus(
                    orderId,
                    dto.getNewStatus(),
                    dto.getChangedBy(),
                    dto.getMemo()
            );
            return ResponseEntity.ok(resp);
        } catch (IllegalStateException | ResourceNotFoundException e) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponseDto(e.getMessage()));
        }
    }

    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<ResponseDto> cancelOrder(
            @PathVariable Long orderId,
            @RequestBody(required = false) CancelOrderRequestDto dto
    ) {
        String reason = (dto != null ? dto.getReason() : null);
        try {
            orderService.cancelOrder(orderId, reason);
            return ResponseEntity.ok(new SuccessResponseDto("주문이 정상적으로 취소되었습니다."));
        } catch (IllegalStateException | ResourceNotFoundException e) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponseDto(e.getMessage()));
        }
    }

    /**
     * 특정 주문의 상태 변경 이력 조회
     */
    @GetMapping("/{orderId}/status-log")
    public ResponseEntity<List<OrderStatusLogDto>> getStatusLog(@PathVariable Long orderId) {
        try {
            List<OrderStatusLogDto> logs = orderService.getStatusLog(orderId);
            return ResponseEntity.ok(logs);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.badRequest()
                    .body(null);
        }
    }

    @PostMapping(path = "/{orderId}/returns")
    public ResponseEntity<?> showReturnsAmount(@PathVariable Long orderId) {

        try {
            int returnsAmount = orderService.requestReturn(orderId);
            return ResponseEntity.ok(returnsAmount);
        } catch (Exception e) {
            ErrorResponseDto error = new ErrorResponseDto(e.getMessage());
            return ResponseEntity.badRequest()
                    .body(error);
        }
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
}