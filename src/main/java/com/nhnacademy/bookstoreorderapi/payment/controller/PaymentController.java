package com.nhnacademy.bookstoreorderapi.payment.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

import com.nhnacademy.bookstoreorderapi.payment.domain.*;
import com.nhnacademy.bookstoreorderapi.payment.domain.entity.Payment;
import com.nhnacademy.bookstoreorderapi.payment.service.PaymentService;

/**
 * 결제 REST 컨트롤러.
 */
@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    /* ─────────── 생성 ─────────── */

    @PostMapping
    public ResponseEntity<Payment> createPayment(@Validated @RequestBody CreatePaymentRequest req) {
        Payment created = paymentService.createPayment(
                req.orderId(),
                req.method(),
                req.provider(),
                req.amount());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /* ─────────── 승인 콜백 ─────────── */

    @PostMapping("/{paymentId}/approve")
    public Payment approve(@PathVariable Long paymentId) {
        return paymentService.approvePayment(paymentId);
    }

    /* ─────────── 취소 ─────────── */

    @PostMapping("/{paymentId}/cancel")
    public Payment cancel(@PathVariable Long paymentId,
                          @Validated @RequestBody CancelPaymentRequest req) {
        return paymentService.cancelPayment(paymentId, req.cancelAmount(), req.reason());
    }

    /* ─────────── 조회 ─────────── */

    @GetMapping("/{paymentId}")
    public Payment get(@PathVariable Long paymentId) {
        return paymentService.getPayment(paymentId);
    }

    @GetMapping
    public List<Payment> search(@RequestParam(required = false) Long orderId,
                                @RequestParam(required = false) PaymentStatus status) {

        if (orderId != null) {
            return paymentService.getPaymentsByOrder(orderId);
        }
        if (status != null) {
            return paymentService.getPaymentsByStatus(status);
        }
        throw new IllegalArgumentException("orderId 또는 status 파라미터 중 하나는 필수입니다.");
    }

    /* ─────────── 요청 DTO ─────────── */

    public record CreatePaymentRequest(
            Long          orderId,
            PaymentMethod method,
            String        provider,
            int           amount) {}

    public record CancelPaymentRequest(
            int    cancelAmount,
            String reason) {}
}