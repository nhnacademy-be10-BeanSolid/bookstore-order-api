package com.nhnacademy.bookstoreorderapi.order.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/fees")
public class DeliveryController {

    public static record FeeRequest(Long userId, int orderAmount) {}

    public static record FeeResponse(int deliveryFee) {}

    @PostMapping
    public ResponseEntity<FeeResponse> calcFee(@RequestBody FeeRequest req) {
        // 스텁 로직: 주문금액 15,000원 이하면 2,500원, 초과면 무료
        int fee = req.orderAmount() <= 15_000 ? 2_500 : 0;
        return ResponseEntity.ok(new FeeResponse(fee));
    }
}