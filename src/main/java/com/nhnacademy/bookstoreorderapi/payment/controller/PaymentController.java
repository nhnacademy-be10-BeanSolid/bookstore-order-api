package com.nhnacademy.bookstoreorderapi.payment.controller;

import com.nhnacademy.bookstoreorderapi.payment.domain.PayType;
import com.nhnacademy.bookstoreorderapi.payment.dto.Request.CancelPaymentRequest;
import com.nhnacademy.bookstoreorderapi.payment.dto.Request.PaymentReqDto;
import com.nhnacademy.bookstoreorderapi.payment.dto.Response.PaymentResDto;
import com.nhnacademy.bookstoreorderapi.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.net.URI;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping(path = "/api/v1/payments", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping(path = "/toss/{orderId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PaymentResDto> requestPayment(
            @PathVariable String orderId,
            @RequestBody @Valid PaymentReqDto dto) {

        PaymentResDto res = paymentService.requestTossPayment(orderId, dto);
        return ResponseEntity
                .created(URI.create("/api/v1/payments/" + res.getPaymentKey()))
                .body(res);
    }

    @GetMapping(path = "/toss/{orderId}/create")
    public ResponseEntity<PaymentResDto> requestPaymentViaGet(
            @PathVariable String orderId,
            @RequestParam PayType payType,
            @RequestParam String payName,
            @RequestParam Long payAmount) {

        PaymentReqDto dto = new PaymentReqDto();
        dto.setPayType(payType);
        dto.setPayName(payName);
        dto.setPayAmount(payAmount);

        PaymentResDto res = paymentService.requestTossPayment(orderId, dto);
        return ResponseEntity
                .created(URI.create("/api/v1/payments/" + res.getPaymentKey()))
                .body(res);
    }

    @GetMapping("/{paymentKey}")
    public ResponseEntity<PaymentResDto> getPaymentInfo(@PathVariable String paymentKey) {
        return ResponseEntity.ok(paymentService.getPaymentInfo(paymentKey));
    }

    @GetMapping("/success")
    public RedirectView tossSuccess(@RequestParam Map<String, String> p) {
        log.info("[PAY CALLBACK] success {}", p);
        String pk = p.get("paymentKey");
        String oid = p.get("orderId");
        Long amt = p.containsKey("amount") ? Long.valueOf(p.get("amount")) : null;
        if (pk != null && oid != null && amt != null) {
            paymentService.markSuccess(pk, oid, amt);
        } else {
            log.warn("필수 파라미터 누락 {}", p);
        }
        RedirectView rv = new RedirectView("/success.html", true);
        p.forEach(rv::addStaticAttribute);
        return rv;
    }

    @GetMapping("/fail")
    public RedirectView tossFail(@RequestParam Map<String, String> p) {
        log.info("[PAY CALLBACK] fail {}", p);
        paymentService.markFail(p.get("paymentKey"), p.get("message"));
        RedirectView rv = new RedirectView("/fail.html", true);
        p.forEach(rv::addStaticAttribute);
        return rv;
    }

    @PostMapping(path = "/{paymentKey}/cancel", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PaymentResDto> cancelPayment(
            @PathVariable String paymentKey,
            @RequestBody @Valid CancelPaymentRequest req) {

        return ResponseEntity.ok(paymentService.refundCardPayment(paymentKey, req));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleAll(Exception ex) {
        log.error("[PAY][ERROR]", ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("status", 500, "message", ex.getMessage()));
    }
}