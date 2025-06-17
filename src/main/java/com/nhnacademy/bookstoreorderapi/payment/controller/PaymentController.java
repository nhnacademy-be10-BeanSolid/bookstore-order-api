package com.nhnacademy.bookstoreorderapi.payment.controller;

import com.nhnacademy.bookstoreorderapi.payment.config.TossPaymentConfig;
import com.nhnacademy.bookstoreorderapi.payment.dto.Request.PaymentReqDto;
import com.nhnacademy.bookstoreorderapi.payment.dto.Response.PaymentResDto;
import com.nhnacademy.bookstoreorderapi.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Validated

public class PaymentController {

    private final PaymentService service;
    @Qualifier("payment.toss-com.nhnacademy.bookstoreorderapi.payment.config.TossPaymentConfig")
    private final TossPaymentConfig tossProps;

    /** 1. 결제 요청 */
    @PostMapping("/toss")
    public ResponseEntity<PaymentResDto> requestPayment(
            @AuthenticationPrincipal User principal,
            @RequestBody PaymentReqDto dto) {

        // (예) 주문 ID 는 프론트 또는 주문 서비스에서 생성해서 전달
        Long orderId = dto.hashCode() & 0xffff_ffffL;   // 샘플용
        var saved = service.saveInitial(dto.toEntity(orderId), principal.getUsername());

        PaymentResDto res = PaymentResDto.builder()
                .orderId(saved.getOrderId())
                .payAmount(saved.getPayAmount())
                .payType(saved.getPayType())
                .orderName(saved.getPayName())
                .successUrl(tossProps.getSuccessUrl())
                .failUrl(tossProps.getFailUrl())
                .build();

        return ResponseEntity.ok(res);
    }

    /** 2. 성공 콜백 */
    @GetMapping("/toss/success")
    public ResponseEntity<Void> success(
            @RequestParam String paymentKey,
            @RequestParam Long   orderId,
            @RequestParam Long   amount) {

        service.markSuccess(paymentKey, orderId, amount);
        return ResponseEntity.ok().build();
    }

    /** 3. 실패 콜백 */
    @GetMapping("/toss/fail")
    public ResponseEntity<Void> fail(
            @RequestParam Long   orderId,
            @RequestParam String message) {

        service.markFail(orderId, message);
        return ResponseEntity.ok().build();
    }
}