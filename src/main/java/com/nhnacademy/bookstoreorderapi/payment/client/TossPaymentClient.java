package com.nhnacademy.bookstoreorderapi.payment.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

/**
 * FeignClient 를 통해 Toss 샌드박스 API 호출
 * 헤더는 TossFeignConfig 에서 자동 주입됩니다.
 */
@FeignClient(
        name          = "toss-payment",
        url           = "${payment.toss.base-url}",
        configuration = com.nhnacademy.bookstoreorderapi.payment.config.TossFeignConfig.class
)
public interface TossPaymentClient {

    @PostMapping("/payments")
    Map<String, Object> createPayment(@RequestBody Map<String, Object> body);

    @PostMapping("/payments/{paymentKey}/confirm")
    void confirmPayment(
            @PathVariable String paymentKey,
            @RequestBody    Map<String, Object> body
    );

    @PostMapping("/payments/{paymentKey}/cancel")
    Map<String, Object> cancelPayment(
            @PathVariable String paymentKey,
            @RequestBody    Map<String, Object> body
    );
}