package com.nhnacademy.bookstoreorderapi.payment.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@FeignClient(
        name = "toss-payment",
        url  = "${payment.toss.base-url}"   // ← application.yml 의 payment.toss.base-url 을 읽어온다
)
public interface TossPaymentClient {

    @PostMapping("/payments")
    ResponseEntity<Map<String, Object>> createPayment(
            @RequestHeader("Authorization") String basicAuth,
            @RequestBody Map<String, Object> body
    );

    @PostMapping("/payments/{paymentKey}/confirm")
    ResponseEntity<Void> confirmPayment(
            @RequestHeader("Authorization") String basicAuth,
            @PathVariable String paymentKey,
            @RequestBody Map<String, Object> body
    );

    @PostMapping("/payments/{paymentKey}/cancel")
    ResponseEntity<Map<String, Object>> cancelPayment(
            @RequestHeader("Authorization") String basicAuth,
            @PathVariable String paymentKey,
            @RequestBody Map<String, Object> body
    );
}