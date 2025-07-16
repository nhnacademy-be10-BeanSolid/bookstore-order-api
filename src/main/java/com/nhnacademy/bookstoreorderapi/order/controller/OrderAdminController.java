package com.nhnacademy.bookstoreorderapi.order.controller;

import com.nhnacademy.bookstoreorderapi.order.dto.response.OrderResponse;
import com.nhnacademy.bookstoreorderapi.order.dto.response.OrderSummaryResponse;
import com.nhnacademy.bookstoreorderapi.order.service.OrderAdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/admin/orders")
@RequiredArgsConstructor
public class OrderAdminController {

    private final OrderAdminService orderAdminService;

    // READ (조회)
    // 모든 회원의 모든 주문 조회
    @GetMapping
    public ResponseEntity<Page<OrderSummaryResponse>> getAllOrders(@RequestHeader("X-USER-ID") String xUserId,
                                                                   Pageable pageable) {
        return ResponseEntity.ok(orderAdminService.getAllOrders(pageable, xUserId));
    }

    // UPDATE (수정)
    // 주문 상태 변경("PENDING" -> "SHIPPING")
    //TODO: "COMPLETED" -> "RETURNED"도 관리자가 변경할 수 있게 구현 예정.
    @PutMapping("/{orderNumber}/status")
    public ResponseEntity<OrderResponse> changeStatusToShipping(@PathVariable String orderNumber,
                                                                @RequestHeader("X-USER-ID") String xUserId) {
        return ResponseEntity.ok(orderAdminService.changeStatusToShipping(orderNumber, xUserId));
    }
}
