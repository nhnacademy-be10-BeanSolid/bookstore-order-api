package com.nhnacademy.bookstoreorderapi.payment.client;

import com.nhnacademy.bookstoreorderapi.payment.config.TossFeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

/**
 * FeignClient 를 통해 Toss 샌드박스 API 호출
 */
@FeignClient(
        name = "toss-payment",
        url  = "${payment.toss.base-url}",       // application.yml 의 payment.toss.base-url
        configuration = TossFeignConfig.class    // ← 여기에 config 클래스 지정
)
public interface TossPaymentClient {

    @PostMapping("/payments")
    ResponseEntity<Map<String, Object>> createPayment(
            @RequestBody Map<String, Object> body
    );

    @PostMapping("/payments/{paymentKey}/confirm")
    ResponseEntity<Void> confirmPayment(
            @PathVariable String paymentKey,
            @RequestBody Map<String, Object> body
    );

    @PostMapping("/payments/{paymentKey}/cancel")
    ResponseEntity<Map<String, Object>> cancelPayment(
            @PathVariable String paymentKey,
            @RequestBody Map<String, Object> body
    );
}