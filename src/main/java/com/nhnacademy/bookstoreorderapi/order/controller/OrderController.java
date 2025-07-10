package com.nhnacademy.bookstoreorderapi.order.controller;

import com.nhnacademy.bookstoreorderapi.order.dto.CancelOrderRequestDto;
import com.nhnacademy.bookstoreorderapi.order.dto.SuccessResponseDto;
import com.nhnacademy.bookstoreorderapi.order.dto.request.OrderRequest;
import com.nhnacademy.bookstoreorderapi.order.dto.request.ReturnRequest;
import com.nhnacademy.bookstoreorderapi.order.dto.request.StatusChangeRequest;
import com.nhnacademy.bookstoreorderapi.order.dto.response.OrderResponse;
import com.nhnacademy.bookstoreorderapi.order.dto.response.OrderSummaryResponse;
import com.nhnacademy.bookstoreorderapi.order.dto.response.PurchaseVerificationResponse;
import com.nhnacademy.bookstoreorderapi.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // 주문 생성(회원, 비회원 둘 다 가능)
    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody OrderRequest orderRequest,
                                                     @RequestHeader("X-USER-ID") String xUserId) {
        OrderResponse order = orderService.createOrder(orderRequest, xUserId);
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

    // 회원 주문 전체 조회
    @GetMapping
    public ResponseEntity<Page<OrderSummaryResponse>> getAllOrdersByUserId(@RequestHeader("X-USER-ID") String xUserId) {
        return ResponseEntity.ok().body(orderService.findAllByUserId(xUserId));
    }

    // 회원 주문 상세 조회
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrder(@RequestHeader("X-USER-ID") String xUserId, @PathVariable String orderId) {
        return ResponseEntity.ok().body(orderService.findByOrderId(orderId, xUserId));
    }

    // 주문 상태 변경
    @PatchMapping("/{orderId}/status")
    public ResponseEntity<OrderResponse> changeOrderStatus(@PathVariable String orderId,
                                                           @RequestBody StatusChangeRequest request,
                                                           @RequestHeader("X-USER-ID") String xUserId) {
        return ResponseEntity.ok(orderService.changeStatus(orderId, request, xUserId));
    }

    @PostMapping("/{orderId}/cancel")
    public SuccessResponseDto cancelOrder(
            @PathVariable String orderId,
            @RequestBody(required = false) CancelOrderRequestDto dto
    ) {
        String reason = (dto != null ? dto.getReason() : null);
        orderService.cancelOrder(orderId, reason);
        return new SuccessResponseDto("주문이 정상적으로 취소되었습니다.");
    }

    //TODO: 사용자만 가능한 주문 상태 변경 api로 리팩토링하기
//    @GetMapping("/{orderId}/status-log")
//    public List<OrderStatusLogDto> getStatusLog(@PathVariable String orderId,
//                                                @RequestHeader("X-USER-ID") String xUserId) {
//        return orderService.getStatusLog(orderId, xUserId);
//    }

    @PostMapping("/{orderId}/returns")
    public ResponseEntity<Integer> requestReturn(@PathVariable String orderId, @RequestBody ReturnRequest dto) {

        int returnsAmount = orderService.requestReturn(orderId, dto);
        return ResponseEntity.ok(returnsAmount);
    }

    @GetMapping("/verify-purchase")
    public ResponseEntity<PurchaseVerificationResponse> verifyPurchase(@RequestHeader("X-USER-ID") String xUserId,
                                                                       @RequestParam Long bookId) {
        return ResponseEntity.ok(orderService.verifyPurchase(xUserId, bookId));
    }
}
