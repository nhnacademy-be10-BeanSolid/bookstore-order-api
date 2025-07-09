package com.nhnacademy.bookstoreorderapi.order.controller;

import com.nhnacademy.bookstoreorderapi.order.dto.request.StatusChangeRequest;
import com.nhnacademy.bookstoreorderapi.order.dto.response.OrderResponse;
import com.nhnacademy.bookstoreorderapi.order.service.OrderAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/orders")
@RequiredArgsConstructor
public class OrderAdminController {

    private final OrderAdminService orderAdminService;

    @PatchMapping("/{orderId}/status")
    public ResponseEntity<OrderResponse> changeOrderStatus(@PathVariable String orderId,
                                                           @RequestBody StatusChangeRequest request,
                                                           @RequestHeader("X-USER-ID") String xUserId) {
        return ResponseEntity.ok(orderAdminService.changeStatus(orderId, request, xUserId));
    }
}
