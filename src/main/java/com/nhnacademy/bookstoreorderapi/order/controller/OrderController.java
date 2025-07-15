package com.nhnacademy.bookstoreorderapi.order.controller;

import com.nhnacademy.bookstoreorderapi.order.common.resolver.XUserIdResolver;
import com.nhnacademy.bookstoreorderapi.order.dto.request.CreateOrderRequest;
import com.nhnacademy.bookstoreorderapi.order.dto.request.ReturnsRequest;
import com.nhnacademy.bookstoreorderapi.order.dto.request.UpdateOrderRequest;
import com.nhnacademy.bookstoreorderapi.order.dto.response.CreateOrderResponse;
import com.nhnacademy.bookstoreorderapi.order.dto.response.OrderDetailResponse;
import com.nhnacademy.bookstoreorderapi.order.dto.response.OrderResponse;
import com.nhnacademy.bookstoreorderapi.order.exception.badrequest.InvalidRequestException;
import com.nhnacademy.bookstoreorderapi.order.exception.unauthorized.NotMemberException;
import com.nhnacademy.bookstoreorderapi.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final XUserIdResolver xUserIdResolver;

    // 주문 생성(회원, 비회원 둘 다 가능)
    @PostMapping
    public ResponseEntity<CreateOrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request,
                                                           @RequestHeader(value = "X-USER-ID", required = false) String xUserId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.createOrder(request, xUserId));
    }

    //TODO: 일정 시간이 지났는데도 완료되지 않으면 주문 데이터를 삭제하게 구현할 예정
    // 주문서 작성 페이지
    @GetMapping("/{orderNumber}/input-detail")
    public ResponseEntity<CreateOrderResponse> getUnfinishedOrder(@PathVariable String orderNumber,
                                                                  @RequestHeader(value = "X-USER-ID", required = false) String xUserId) {
        validateOrderNumber(orderNumber);
        return ResponseEntity.ok(orderService.getUnfinishedOrder(orderNumber, xUserId));
    }

    // 주문 업데이트(포장 및 배송정보 업데이트)
    @PutMapping("/{orderNumber}")
    public ResponseEntity<OrderResponse> updateOrder(@PathVariable String orderNumber,
                                                     @Valid @RequestBody UpdateOrderRequest request,
                                                     @RequestHeader(value = "X-USER-ID", required = false) String xUserId) {
        validateOrderNumber(orderNumber);
        return ResponseEntity.ok(orderService.updateOrder(orderNumber, request, xUserId));
    }
//
//    // 회원 주문 전체 조회
//    @GetMapping
//    public ResponseEntity<Page<OrderSummaryResponse>> getAllOrdersByUserId(@RequestHeader("X-USER-ID") String xUserId,
//                                                                           Pageable pageable) {
//        return ResponseEntity.ok().body(orderService.findAllByUserId(xUserId, pageable));
//    }
//
    // 주문 상세 조회
    @GetMapping("/{orderNumber}")
    public ResponseEntity<OrderDetailResponse> getOrder(@PathVariable String orderNumber,
                                                        @RequestHeader(value = "X-USER-ID", required = false) String xUserId) {
        validateOrderNumber(orderNumber);
        return ResponseEntity.ok().body(orderService.findByOrderNumber(orderNumber, xUserId));
    }

    // 반품 요청
    @PutMapping("/{orderNumber}/status")
    public ResponseEntity<OrderResponse> requestReturns(@PathVariable String orderNumber,
                                                        @Valid @RequestBody ReturnsRequest request,
                                                        @RequestHeader("X-USER-ID") String xUserId) {
        validateOrderNumber(orderNumber);
        Long userNo = xUserIdResolver.resolveUserNo(xUserId);
        if (userNo == null) {
            log.warn("[경고] 비회원이 반품 기능에 접근함");
            throw new NotMemberException("회원이 아닙니다");
        }

        return ResponseEntity.ok(orderService.changeStatusToReturned(orderNumber, request, userNo));
    }
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
    private void validateOrderNumber(String orderNumber) {
        if (orderNumber.isBlank()) {
            throw new InvalidRequestException("주문번호가 비어있습니다.");
        }
    }
}
