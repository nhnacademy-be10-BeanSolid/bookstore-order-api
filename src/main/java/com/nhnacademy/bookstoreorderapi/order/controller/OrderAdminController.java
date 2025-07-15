package com.nhnacademy.bookstoreorderapi.order.controller;

import com.nhnacademy.bookstoreorderapi.order.common.resolver.XUserIdResolver;
import com.nhnacademy.bookstoreorderapi.order.dto.response.OrderResponse;
import com.nhnacademy.bookstoreorderapi.order.dto.response.OrderSummaryResponse;
import com.nhnacademy.bookstoreorderapi.order.exception.forbidden.NotAdminException;
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
    private final XUserIdResolver xUserIdResolver;

    @GetMapping
    public ResponseEntity<Page<OrderSummaryResponse>> getAllOrders(@RequestHeader("X-USER-ID") String xUserId,
                                                                   Pageable pageable) {
        validateAdminAccess(xUserId);
        return ResponseEntity.ok(orderAdminService.getAllOrders(pageable));
    }

    @PutMapping("/{orderNumber}/status")
    public ResponseEntity<OrderResponse> changeStatusToShipping(@PathVariable String orderNumber,
                                                                @RequestHeader("X-USER-ID") String xUserId) {
        validateAdminAccess(xUserId);
        return ResponseEntity.ok(orderAdminService.changeStatusToShipping(orderNumber, xUserId));
    }

    private void validateAdminAccess(String xUserId) {
        if (!xUserIdResolver.isAdmin(xUserId)) {
            log.warn("관리자가 아닌 사용자가 접근했습니다: xUserId={}", xUserId);
            throw new NotAdminException("관리자 권한이 필요합니다.");
        }
    }
}
