package com.nhnacademy.bookstoreorderapi.order.controller;

import com.nhnacademy.bookstoreorderapi.order.dto.CancelOrderRequestDto;
import com.nhnacademy.bookstoreorderapi.order.dto.OrderRequestDto;
import com.nhnacademy.bookstoreorderapi.order.dto.OrderResponseDto;
import com.nhnacademy.bookstoreorderapi.order.dto.OrderStatusLogDto;
import com.nhnacademy.bookstoreorderapi.order.dto.StatusChangeRequestDto;
import com.nhnacademy.bookstoreorderapi.order.dto.StatusChangeResponseDto;
import com.nhnacademy.bookstoreorderapi.order.dto.SuccessResponseDto;
import com.nhnacademy.bookstoreorderapi.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @GetMapping
    public List<OrderResponseDto> listAll() {
        return orderService.listAll();
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
    public int requestReturn(@PathVariable Long orderId) {
        return orderService.requestReturn(orderId);
    }

    private void validateOrderType(OrderRequestDto req) {
        String type = req.getOrderType();
        if ("guest".equalsIgnoreCase(type)) {
            if (req.getGuestName() == null || req.getGuestPhone() == null) {
                throw new IllegalArgumentException("비회원 주문은 이름과 전화번호가 필요합니다.");
            }
        } else if ("member".equalsIgnoreCase(type)) {
            if (req.getUserId() == null) {
                throw new IllegalArgumentException("회원 주문은 userId가 필요합니다.");
            }
        } else {
            throw new IllegalArgumentException("orderType은 'member' 또는 'guest'여야 합니다.");
        }
    }
}