// src/main/java/com/nhnacademy/bookstoreorderapi/payment/client/TossPaymentClient.java
package com.nhnacademy.bookstoreorderapi.payment.client;

import com.nhnacademy.bookstoreorderapi.payment.config.TossFeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@FeignClient(
        name          = "toss-payment",
        url           = "${payment.toss.base-url}",
        configuration = TossFeignConfig.class
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