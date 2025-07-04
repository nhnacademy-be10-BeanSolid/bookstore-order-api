package com.nhnacademy.bookstoreorderapi.order.controller;

import com.nhnacademy.bookstoreorderapi.order.dto.*;
import com.nhnacademy.bookstoreorderapi.order.dto.request.OrderRequest;
import com.nhnacademy.bookstoreorderapi.order.dto.request.ReturnRequest;
import com.nhnacademy.bookstoreorderapi.order.dto.request.StatusChangeRequest;
import com.nhnacademy.bookstoreorderapi.order.dto.response.OrderResponse;
import com.nhnacademy.bookstoreorderapi.order.dto.response.OrderSummaryResponse;
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

    // 주문 생성(회원, 비회원 둘 다 가능)
    @PostMapping
    public ResponseEntity<Void> createOrder(@Valid @RequestBody OrderRequest orderRequest,
                                            @RequestHeader("X-USER-ID") String xUserId) {
        orderService.createOrder(orderRequest, xUserId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    // 회원 주문 전체 조회
    @GetMapping
    public ResponseEntity<List<OrderSummaryResponse>> getAllOrdersByUserId(@RequestHeader("X-USER-ID") String xUserId) {
        return ResponseEntity.ok().body(orderService.findAllByUserId(xUserId));
    }

    // 회원 주문 상세 조회
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrder(@RequestHeader("X-USER-ID") String xUserId, @PathVariable String orderId) {
        return ResponseEntity.ok().body(orderService.findByOrderId(orderId, xUserId));
    }

    // 주문 상태 변경
    @PatchMapping("/{orderId}/status")
    public StatusChangeResponseDto changeStatus(@PathVariable String orderId,
                                                @Valid @RequestBody StatusChangeRequest dto,
                                                @RequestHeader("X-USER-ID") String xUserId) {
        return orderService.changeStatus(
                orderId,
                dto.newStatus(),
                dto.memo(),
                xUserId
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
    public List<OrderStatusLogDto> getStatusLog(@PathVariable String orderId,
                                                @RequestHeader("X-USER-ID") String xUserId) {
        return orderService.getStatusLog(orderId, xUserId);
    }

    @PostMapping("/{orderId}/returns")
    public ResponseEntity<Integer> requestReturn(@PathVariable String orderId, @RequestBody ReturnRequest dto) {

        int returnsAmount = orderService.requestReturn(orderId, dto);
        return ResponseEntity.ok(returnsAmount);
    }
}
