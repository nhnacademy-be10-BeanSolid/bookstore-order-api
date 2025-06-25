package com.nhnacademy.bookstoreorderapi.payment.controller;

import com.nhnacademy.bookstoreorderapi.payment.dto.Request.PaymentReqDto;
import com.nhnacademy.bookstoreorderapi.payment.dto.Response.PaymentResDto;
import com.nhnacademy.bookstoreorderapi.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.net.URI;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping(path = "/api/v1/payments", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService; // 서비스 주입

    // 1) 결제창(인증) 생성
    @CrossOrigin(origins = "*")
    @PostMapping(path = "/toss/{orderId}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PaymentResDto> requestPayment(
            @PathVariable String orderId,
            @RequestBody @Valid PaymentReqDto dto) {

        log.info("[PAY] requestPayment: orderId={} dto={}", orderId, dto);
        PaymentResDto result = paymentService.requestTossPayment(orderId, dto);
        return ResponseEntity.created(URI.create("/api/v1/payments/" + result.getPaymentId()))
                .body(result);
    }

    // 2) 성공 콜백
    @GetMapping("/toss/success")
    public RedirectView tossSuccess(@RequestParam String paymentKey,
                                    @RequestParam String orderId,
                                    @RequestParam Long amount,
                                    @RequestParam(required = false) String paymentType) {

        log.info("[PAY CALLBACK] success: orderId={} key={} amount={} type={}",
                orderId, paymentKey, amount, paymentType);
        paymentService.markSuccess(paymentKey, orderId, amount);
        return new RedirectView("/success.html"); // 성공 페이지로 이동
    }

    // 3) 실패 콜백
    @GetMapping("/toss/fail")
    public RedirectView tossFail(@RequestParam Map<String, String> params) {

        log.info("[PAY CALLBACK] fail: {}", params);
        paymentService.markFail(params.get("paymentKey"), params.get("message"));
        return new RedirectView("/fail.html"); // 실패 페이지로 이동
    }

    // 4) 포인트 환불(취소)
    @PostMapping(path = "/toss/cancel/point",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<Map<String, Object>> cancelPaymentPoint(
            @RequestParam String paymentKey,
            @RequestParam String cancelReason) {

        log.info("[PAY CANCEL] key={} reason={}", paymentKey, cancelReason);
        Map<String, Object> resp = paymentService.cancelPaymentPoint(paymentKey, cancelReason);
        return ResponseEntity.ok(resp);
    }

    // 예외 처리 공통
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleAll(Exception ex) {

        log.error("[PAY][ERROR] ", ex);
        Map<String, Object> body = Map.of(
                "timestamp", System.currentTimeMillis(),
                "status", HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "error", HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                "message", ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}