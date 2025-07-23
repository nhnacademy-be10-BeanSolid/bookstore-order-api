package com.nhnacademy.bookstoreorderapi.order.controller;

import com.nhnacademy.bookstoreorderapi.order.controller.swagger.OrderControllerDocs;
import com.nhnacademy.bookstoreorderapi.order.dto.request.CreateOrderRequest;
import com.nhnacademy.bookstoreorderapi.order.dto.request.OrderStatusRequest;
import com.nhnacademy.bookstoreorderapi.order.dto.request.UpdateOrderRequest;
import com.nhnacademy.bookstoreorderapi.order.dto.response.*;
import com.nhnacademy.bookstoreorderapi.order.exception.badrequest.InvalidRequestException;
import com.nhnacademy.bookstoreorderapi.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController implements OrderControllerDocs {

    private final OrderService orderService;

    // CREATE (생성)
    // 주문 생성(회원, 비회원 둘 다 가능)
    @PostMapping
    public ResponseEntity<CreateOrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request,
                                                           @RequestHeader(value = "X-USER-ID", required = false) String xUserId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.createOrder(request, xUserId));
    }

    // READ (조회)
    // 주문서 작성 페이지
    @GetMapping("/{orderNumber}/input-detail")
    public ResponseEntity<CreateOrderResponse> getUnfinishedOrder(@PathVariable String orderNumber, //TODO: 일정 시간이 지났는데도 완료되지 않으면 주문 데이터를 삭제하게 구현할 예정
                                                                  @RequestHeader(value = "X-USER-ID", required = false) String xUserId) {
        validateOrderNumber(orderNumber);
        return ResponseEntity.ok(orderService.getUnfinishedOrder(orderNumber, xUserId));
    }

    // 회원 주문 전체 조회
    @GetMapping
    public ResponseEntity<Page<OrderSummaryResponse>> getAllOrdersByUserId(@RequestHeader("X-USER-ID") String xUserId,
                                                                           Pageable pageable) {
        return ResponseEntity.ok().body(orderService.findAllByUserId(xUserId, pageable));
    }

    // 주문 상세 조회
    @GetMapping("/{orderNumber}")
    public ResponseEntity<OrderDetailResponse> getOrder(@PathVariable String orderNumber,
                                                        @RequestHeader(value = "X-USER-ID", required = false) String xUserId) {
        validateOrderNumber(orderNumber);
        return ResponseEntity.ok().body(orderService.findByOrderNumber(orderNumber, xUserId));
    }

    // UPDATE (수정)
    // 주문 업데이트(포장 및 배송정보 업데이트)
    @PutMapping("/{orderNumber}")
    public ResponseEntity<OrderResponse> updateOrder(@PathVariable String orderNumber,
                                                     @Valid @RequestBody UpdateOrderRequest request,
                                                     @RequestHeader(value = "X-USER-ID", required = false) String xUserId) {
        validateOrderNumber(orderNumber);
        return ResponseEntity.ok(orderService.updateOrder(orderNumber, request, xUserId));
    }

    // 주문 상태 변경(반품, 취소)
    @PutMapping("/{orderNumber}/status")
    public ResponseEntity<OrderStatusResult> changeOrderStatus(@PathVariable String orderNumber,
                                               @Valid @RequestBody OrderStatusRequest request,
                                               @RequestHeader("X-USER-ID") String xUserId) {
        validateOrderNumber(orderNumber);
        OrderStatusResult result = orderService.changeOrderStatus(orderNumber, request, xUserId);
        
        return ResponseEntity.ok(result);
    }

    private void validateOrderNumber(String orderNumber) {
        if (orderNumber.isBlank()) {
            throw new InvalidRequestException("주문번호가 비어있습니다.");
        }
    }
}
