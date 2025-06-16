package com.nhnacademy.bookstoreorderapi.order.controller;

import com.nhnacademy.bookstoreorderapi.order.dto.*;
import com.nhnacademy.bookstoreorderapi.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @GetMapping
    public List<OrderResponseDto> listMyOrders(@RequestParam String userId) {
        return orderService.listByUser(userId);
    }

    @PostMapping//수정 - 각 API가 자신의 구체적인 DTO만 책임지도록!
    public OrderResponseDto createOrder(@Valid @RequestBody OrderRequestDto req) {
        validateOrderType(req);
        return orderService.createOrder(req);
    }

    @PatchMapping("/{orderId}/status")
    public StatusChangeResponseDto changeStatus(
            @PathVariable Long orderId,
            @Valid @RequestBody StatusChangeRequestDto dto
    ) {
        return orderService.changeStatus(
                orderId,
                dto.getNewStatus(),
                dto.getChangedBy(),
                dto.getMemo()
        );
    }

    @PostMapping("/{orderId}/cancel")
    public SuccessResponseDto cancelOrder(
            @PathVariable Long orderId,
            @RequestBody(required = false) CancelOrderRequestDto dto
    ) {
        String reason = (dto != null ? dto.getReason() : null);
        orderService.cancelOrder(orderId, reason);
        return new SuccessResponseDto("주문이 정상적으로 취소되었습니다.");
    }

    @GetMapping("/{orderId}/status-log")
    public List<OrderStatusLogDto> getStatusLog(@PathVariable Long orderId) {
        return orderService.getStatusLog(orderId);
    }

    @PostMapping("/{orderId}/returns")
    public ResponseEntity<Integer> requestReturn(@PathVariable Long orderId, @RequestBody ReturnRequestDto dto) {

        int returnsAmount = orderService.requestReturn(orderId, dto);
        return ResponseEntity.ok(returnsAmount);
    }

    private void validateOrderType(OrderRequestDto req) {
        switch (req.getOrderType().toLowerCase()) {
            case "guest" -> {
                if (req.getGuestId() == null) throw new IllegalArgumentException("guestId 필요");
            }
            case "member" -> {
                if (req.getUserId() == null) throw new IllegalArgumentException("userId 필요");
            }
            default -> throw new IllegalArgumentException("orderType 은 member | guest 만 허용");
        }
    }
}