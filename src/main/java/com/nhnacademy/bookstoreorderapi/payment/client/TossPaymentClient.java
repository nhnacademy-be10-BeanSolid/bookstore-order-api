package com.nhnacademy.bookstoreorderapi.payment.client;

import com.nhnacademy.bookstoreorderapi.payment.config.TossFeignConfig;
import com.nhnacademy.bookstoreorderapi.payment.dto.Request.PaymentApprovalRequestDto;
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

    @PostMapping("/payments/confirm")
    PaymentApprovalRequestDto confirmPayment(
            PaymentApprovalRequestDto dto
    );

    @PostMapping("/payments/{paymentKey}/cancel")
    Map<String,Object> cancelPayment(
            @PathVariable String paymentKey,
            @RequestBody Map<String,Object> body
    );

    @GetMapping("/payments/{paymentKey}")
    Map<String,Object> getPaymentInfo(@PathVariable String paymentKey);
}