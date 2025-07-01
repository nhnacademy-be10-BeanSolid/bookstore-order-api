package com.nhnacademy.bookstoreorderapi.payment.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
@FeignClient(name="toss", url="${payment.toss.base-url}")
public interface TossPaymentClient {
    @PostMapping("/payments")

    Map<String,Object> createPayment(@RequestBody Map<String,Object> body);

    @PostMapping("/payments/{paymentKey}/confirm")
    void confirmPayment(@PathVariable String paymentKey,
                        @RequestBody Map<String,Object> body);

    @PostMapping("/payments/{paymentKey}/cancel")
    Map<String,Object> cancelPayment(@PathVariable String paymentKey,
                                     @RequestBody Map<String,Object> body);
}