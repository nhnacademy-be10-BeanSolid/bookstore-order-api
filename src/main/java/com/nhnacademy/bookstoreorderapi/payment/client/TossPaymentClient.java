// src/main/java/com/nhnacademy/bookstoreorderapi/payment/client/TossPaymentClient.java
package com.nhnacademy.bookstoreorderapi.payment.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(
        name = "toss-payment",
        url  = "${payment.toss.base-url}",      // https://sandbox.tosspayments.com/v1
        configuration = com.nhnacademy.bookstoreorderapi.payment.config.TossFeignConfig.class
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