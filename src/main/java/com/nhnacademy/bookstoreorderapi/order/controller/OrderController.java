package com.nhnacademy.bookstoreorderapi.order.controller;

import com.nhnacademy.bookstoreorderapi.order.dto.*;
import com.nhnacademy.bookstoreorderapi.order.dto.request.OrderRequest;
import com.nhnacademy.bookstoreorderapi.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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

    //TODO 99: @ControllerAdvice로 값검증 400에러로 전역 처리하기.
    @PostMapping
    public ResponseEntity<Void> createOrder(@Valid @RequestBody OrderRequest orderRequest, @RequestHeader("X-User-Id") String userId){
        orderService.createOrder(orderRequest, userId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
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
}
