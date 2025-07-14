package com.nhnacademy.bookstoreorderapi.order.controller;

import com.nhnacademy.bookstoreorderapi.order.dto.response.UserOrderAmountResponse;
import com.nhnacademy.bookstoreorderapi.order.service.OrderInternalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 내부 서비스 간 통신할 때 쓰는 api를 모아놓은 클래스입니다.
 */
@RestController
@RequestMapping("/internal/orders")
@RequiredArgsConstructor
public class OrderInternalController {

    private final OrderInternalService orderInternalService;

    @GetMapping
    public ResponseEntity<List<UserOrderAmountResponse>> getOrderAmountGroupByUserLastThreeMonth() {
        return ResponseEntity.ok(orderInternalService.findOrderAmountGroupByUserLastThreeMonths());
    }

    @GetMapping("/{orderNumber}")
    public ResponseEntity<Long> getIdByOrderNumber(@PathVariable String orderNumber) {
        return ResponseEntity.ok(orderInternalService.findIdByOrderNumber(orderNumber));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<String> getOrderNumberById(@PathVariable Long orderId) {
        return ResponseEntity.ok(orderInternalService.findOrderNumberById(orderId));
    }
}
