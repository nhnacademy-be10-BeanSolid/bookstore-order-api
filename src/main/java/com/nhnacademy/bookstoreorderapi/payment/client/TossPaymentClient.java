package com.nhnacademy.bookstoreorderapi.payment.client;

import com.nhnacademy.bookstoreorderapi.payment.config.TossFeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@FeignClient(
        name = "toss-client",
        url = "${payment.toss.base-url}",
        configuration = TossFeignConfig.class
)
public interface TossPaymentClient {

    // 결제 생성
    @PostMapping("/payments")
    Map<String, Object> createPayment(@RequestBody Map<String, Object> body);

    // 결제 승인 – paymentKey를 path 로 넣는다
    @PostMapping("/payments/{paymentKey}")
    Map<String, Object> confirmPayment(
            @PathVariable String paymentKey,
            @RequestBody  Map<String, Object> body);

    // 환불
    @PostMapping("/payments/{paymentKey}/cancel")
    Map<String, Object> cancelPayment(
            @PathVariable String paymentKey,
            @RequestBody  Map<String, Object> body);

    // 결제 조회
    @GetMapping("/payments/{paymentKey}")
    Map<String, Object> getPaymentInfo(@PathVariable String paymentKey);
}