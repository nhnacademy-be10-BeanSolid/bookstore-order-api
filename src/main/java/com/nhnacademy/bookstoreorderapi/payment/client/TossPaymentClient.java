package com.nhnacademy.bookstoreorderapi.payment.client;

import com.nhnacademy.bookstoreorderapi.payment.config.TossFeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
@FeignClient(
        name = "toss-client",
        url  = "${payment.toss.base-url}",
        configuration = TossFeignConfig.class
)
public interface TossPaymentClient {

    @PostMapping("/payments")
    Map<String,Object> createPayment(@RequestBody Map<String,Object> body);

    // ✔ paymentKey 를 path 로 넣지 않는다
    @PostMapping("/payments/confirm")
    Map<String,Object> confirmPayment(@RequestBody Map<String,Object> body);

    @PostMapping("/payments/{paymentKey}/cancel")
    Map<String,Object> cancelPayment(
            @PathVariable String paymentKey,
            @RequestBody Map<String,Object> body);

    @GetMapping("/payments/{paymentKey}")
    Map<String,Object> getPaymentInfo(@PathVariable String paymentKey);
}