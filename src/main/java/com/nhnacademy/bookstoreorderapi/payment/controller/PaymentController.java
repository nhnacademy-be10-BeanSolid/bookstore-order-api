package com.nhnacademy.bookstoreorderapi.payment.controller;

import com.nhnacademy.bookstoreorderapi.payment.config.TossPaymentConfig;
import com.nhnacademy.bookstoreorderapi.payment.dto.Request.PaymentReqDto;
import com.nhnacademy.bookstoreorderapi.payment.dto.Response.PaymentResDto;
import com.nhnacademy.bookstoreorderapi.payment.domain.entity.Payment;
import com.nhnacademy.bookstoreorderapi.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/vi/payments/toss")
@RequiredArgsConstructor
@Validated
public class PaymentController {

    private final PaymentService   service;
    private final TossPaymentConfig tossProps;

    /* ───────── 1. 결제 요청 ───────── */
    @PostMapping(value = "/toss", consumes = "application/json", produces = "application/json")
    public ResponseEntity<PaymentResDto> requestPayment(
            @AuthenticationPrincipal(expression = "username") String email,
            @RequestBody @Valid PaymentReqDto dto) {

        long orderId = System.currentTimeMillis();                // 샘플: “안전한” 주문번호
        Payment saved = service.saveInitial(dto.toEntity(orderId), email);

        return ResponseEntity.ok(
                PaymentResDto.builder()
                        .paymentId(saved.getPaymentId())
                        .orderId(saved.getOrderId())
                        .payAmount(saved.getPayAmount())
                        .payType(saved.getPayType())
                        .orderName(saved.getPayName())
                        .successUrl(tossProps.getSuccessUrl())
                        .failUrl(tossProps.getFailUrl())
                        .build());
    }

    /* ───────── 2. 성공 콜백 ───────── */
    @GetMapping("/toss/success")
    public ResponseEntity<Void> success(@RequestParam String paymentKey,
                                        @RequestParam Long   orderId,
                                        @RequestParam Long   amount) {

        service.markSuccess(paymentKey, orderId, amount);
        return ResponseEntity.ok().build();
    }

    /* ───────── 3. 실패 콜백 ───────── */
    @GetMapping("/toss/fail")
    public ResponseEntity<Void> fail(@RequestParam Long   orderId,
                                     @RequestParam String message) {

        service.markFail(orderId, message);
        return ResponseEntity.ok().build();
    }
}