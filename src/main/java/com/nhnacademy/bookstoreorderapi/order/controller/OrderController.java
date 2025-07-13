package com.nhnacademy.bookstoreorderapi.order.controller;

import com.nhnacademy.bookstoreorderapi.order.dto.CancelOrderRequestDto;
import com.nhnacademy.bookstoreorderapi.order.dto.SuccessResponseDto;
import com.nhnacademy.bookstoreorderapi.order.dto.request.CreateOrderRequest;
import com.nhnacademy.bookstoreorderapi.order.dto.request.ReturnRequest;
import com.nhnacademy.bookstoreorderapi.order.dto.request.StatusChangeRequest;
import com.nhnacademy.bookstoreorderapi.order.dto.response.*;
import com.nhnacademy.bookstoreorderapi.order.exception.InvalidRequestException;
import com.nhnacademy.bookstoreorderapi.order.service.OrderService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // 주문 생성(회원, 비회원 둘 다 가능)
    @PostMapping
    public ResponseEntity<CreateOrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request,
                                                           @RequestHeader(value = "X-USER-ID", required = false) String xUserId) {
        CreateOrderResponse order = orderService.createOrder(request, xUserId);
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

    // 주문서 작성 페이지
    @GetMapping("/{orderNumber}/input-detail")
    public ResponseEntity<CreateOrderResponse> getUnfinishedOrder(@PathVariable
                                                                  @NotNull(message = "주문번호는 필수입니다")
                                                                  @Pattern(regexp = "^\\d{6}-[a-zA-Z0-9]{6}-[a-zA-Z0-9]{6}$",
                                                                          message = "주문번호 형식이 올바르지 않습니다")
                                                                  String orderNumber,
                                                                  @RequestHeader(value = "X-USER-ID", required = false) String xUserId) {
        return ResponseEntity.ok(orderService.getUnfinishedOrder(orderNumber, xUserId));
    }
//
//    // 회원 주문 전체 조회
//    @GetMapping
//    public ResponseEntity<Page<OrderSummaryResponse>> getAllOrdersByUserId(@RequestHeader("X-USER-ID") String xUserId,
//                                                                           Pageable pageable) {
//        return ResponseEntity.ok().body(orderService.findAllByUserId(xUserId, pageable));
//    }
//
//    // 회원 주문 상세 조회
//    @GetMapping("/{orderId}")
//    public ResponseEntity<OrderDetailResponse> getOrder(@RequestHeader("X-USER-ID") String xUserId,
//                                                        @PathVariable String orderId) {
//        return ResponseEntity.ok().body(orderService.findByOrderId(xUserId, orderId));
//    }

//    // 주문 상태 변경
//    @PatchMapping("/{orderId}/status")
//    public ResponseEntity<OrderResponse> changeOrderStatus(@PathVariable String orderId,
//                                                           @RequestBody StatusChangeRequest request,
//                                                           @RequestHeader("X-USER-ID") String xUserId) {
//        return ResponseEntity.ok(orderService.changeStatus(orderId, request, xUserId));
//    }
//
//    @PostMapping("/{orderId}/cancel")
//    public SuccessResponseDto cancelOrder(
//            @PathVariable String orderId,
//            @RequestBody(required = false) CancelOrderRequestDto dto
//    ) {
//        String reason = (dto != null ? dto.getReason() : null);
//        orderService.cancelOrder(orderId, reason);
//        return new SuccessResponseDto("주문이 정상적으로 취소되었습니다.");
//    }

    //TODO: 사용자만 가능한 주문 상태 변경 api로 리팩토링하기
//    @GetMapping("/{orderId}/status-log")
//    public List<OrderStatusLogDto> getStatusLog(@PathVariable String orderId,
//                                                @RequestHeader("X-USER-ID") String xUserId) {
//        return orderService.getStatusLog(orderId, xUserId);
//    }
//
//    @PostMapping("/{orderId}/returns")
//    public ResponseEntity<Integer> requestReturn(@PathVariable String orderId, @RequestBody ReturnRequest dto) {
//
//        int returnsAmount = orderService.requestReturn(orderId, dto);
//        return ResponseEntity.ok(returnsAmount);
//    }
//
//    @GetMapping("/verify-purchase")
//    public ResponseEntity<PurchaseVerificationResponse> verifyPurchase(@RequestHeader("X-USER-ID") String xUserId,
//                                                                       @RequestParam Long bookId) {
//        return ResponseEntity.ok(orderService.verifyPurchase(xUserId, bookId));
//    }
}
