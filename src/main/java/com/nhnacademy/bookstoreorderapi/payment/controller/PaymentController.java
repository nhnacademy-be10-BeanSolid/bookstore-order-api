package com.nhnacademy.bookstoreorderapi.payment.controller;

import com.nhnacademy.bookstoreorderapi.payment.domain.PayType;
import com.nhnacademy.bookstoreorderapi.payment.dto.Request.CancelPaymentRequest;
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

    private final PaymentService paymentService;

    // (1) JSON 바디로 Toss 결제 요청
    @CrossOrigin(origins = "*")
    @PostMapping(path = "/toss/{orderId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PaymentResDto> requestPayment(
            @PathVariable String orderId,
            @RequestBody @Valid PaymentReqDto dto) {

        dto.setOrderId(orderId);              // @NotNull 충족
        PaymentResDto res = paymentService.requestTossPayment(orderId, dto);

        return ResponseEntity
                .created(URI.create("/api/v1/payments/" + res.getPaymentKey()))
                .body(res);
    }

    // (2) GET 파라미터로 Toss 결제 요청
    @CrossOrigin(origins = "*")
    @GetMapping(path = "/toss/{orderId}/create")
    public ResponseEntity<PaymentResDto> requestPaymentViaGet(
            @PathVariable String orderId,
            @RequestParam PayType payType,
            @RequestParam String  payName,
            @RequestParam Long    payAmount) {

        PaymentReqDto dto = new PaymentReqDto();
        dto.setOrderId(orderId);              // 필수
        dto.setPayType(payType);
        dto.setPayName(payName);
        dto.setPayAmount(payAmount);

        PaymentResDto res = paymentService.requestTossPayment(orderId, dto);

        return ResponseEntity
                .created(URI.create("/api/v1/payments/" + res.getPaymentKey()))
                .body(res);
    }

    // (3) 결제 정보 단건 조회
    @GetMapping("/{paymentKey}")
    public ResponseEntity<PaymentResDto> getPaymentInfo(@PathVariable String paymentKey) {
        return ResponseEntity.ok(paymentService.getPaymentInfo(paymentKey));
    }

    // (4) 결제 성공 콜백
    @GetMapping("/toss/success")
    public RedirectView tossSuccess(@RequestParam Map<String,String> params) {
        log.info("[PAY CALLBACK] success: {}", params);

        String pk  = params.get("paymentKey");
        String oid = params.get("orderId");
        Long   amt = params.containsKey("amount") ? Long.valueOf(params.get("amount")) : null;

        if (pk != null && oid != null && amt != null) {
            paymentService.markSuccess(pk, oid, amt);
        } else {
            log.warn("필수 파라미터 누락 : {}", params);
        }
        return redirect("https://bookstore-beansolid.store/payments/success", params);
    }

    // (5) 결제 실패 콜백
    @GetMapping("/toss/fail")
    public RedirectView tossFail(@RequestParam Map<String,String> params) {
        log.info("[PAY CALLBACK] fail: {}", params);
        paymentService.markFail(params.get("paymentKey"), params.get("message"));

        return redirect("https://bookstore-beansolid.store/payments/fail", params);
    }

    // (6) 레거시 환불(Map 버전)
    @PostMapping(path = "/toss/{paymentKey}/refund", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String,Object>> refundCardPaymentLegacy(
            @PathVariable String paymentKey,
            @RequestBody Map<String,Object> req) {

        log.info("[PAY REFUND-LEGACY] key={} req={}", paymentKey, req);
        return ResponseEntity.ok(paymentService.refundCardPayment(paymentKey, req));
    }

    // (7) 결제 취소/환불
    @PostMapping(path = "/{paymentKey}/cancel", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PaymentResDto> cancelPayment(
            @PathVariable String paymentKey,
            @RequestBody @Valid CancelPaymentRequest req) {

        log.info("[PAY CANCEL] key={} req={}", paymentKey, req);
        return ResponseEntity.ok(paymentService.refundCardPayment(paymentKey, req));
    }

    // 공통 예외 처리
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String,Object>> handleAll(Exception ex) {
        log.error("[PAY][ERROR]", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("status", 500, "message", ex.getMessage()));
    }

    // 리다이렉트 헬퍼
    private RedirectView redirect(String url, Map<String,String> params) {
        RedirectView rv = new RedirectView(url);
        params.forEach(rv::addStaticAttribute);
        return rv;
    }
}