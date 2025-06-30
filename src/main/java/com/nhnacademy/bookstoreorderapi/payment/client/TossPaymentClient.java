package com.nhnacademy.bookstoreorderapi.payment.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@FeignClient(
        name = "toss-payment",
        url  = "${payment.toss.base-url}"
)
public interface TossPaymentClient {

    @PostMapping("/payments")
    ResponseEntity<Map<String, Object>> createPayment(
            @RequestHeader("Authorization")    String basicAuth,
            @RequestHeader("X-Client-Api-Key") String clientApiKey,
            @RequestBody                       Map<String, Object> body
    );

    @PostMapping("/payments/{paymentKey}/confirm")
    ResponseEntity<Void> confirmPayment(
            @RequestHeader("Authorization")    String basicAuth,
            @RequestHeader("X-Client-Api-Key") String clientApiKey,
            @PathVariable                      String paymentKey,
            @RequestBody                       Map<String, Object> body
    );

    @PostMapping("/payments/{paymentKey}/cancel")
    ResponseEntity<Map<String, Object>> cancelPayment(
            @RequestHeader("Authorization")    String basicAuth,
            @RequestHeader("X-Client-Api-Key") String clientApiKey,
            @PathVariable                      String paymentKey,
            @RequestBody                       Map<String, Object> body
    );
}