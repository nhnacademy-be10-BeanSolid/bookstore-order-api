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


    @CrossOrigin(origins = "*")
    @PostMapping(path = "/toss/{orderId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PaymentResDto> requestPayment(@PathVariable String orderId,
                                                        @RequestBody @Valid PaymentReqDto dto) {

        dto.setOrderId(orderId);
        PaymentResDto res = paymentService.requestTossPayment(orderId, dto);
        return ResponseEntity.created(URI.create("/api/v1/payments/" + res.getPaymentKey()))
                .body(res);
    }

    @CrossOrigin(origins = "*")
    @GetMapping(path = "/toss/{orderId}/create")
    public ResponseEntity<PaymentResDto> requestPaymentViaGet(@PathVariable String orderId,
                                                              @RequestParam PayType payType,
                                                              @RequestParam String  payName,
                                                              @RequestParam Long    payAmount) {

        PaymentReqDto dto = new PaymentReqDto();
        dto.setOrderId(orderId);
        dto.setPayType(payType);
        dto.setPayName(payName);
        dto.setPayAmount(payAmount);

        PaymentResDto res = paymentService.requestTossPayment(orderId, dto);
        return ResponseEntity.created(URI.create("/api/v1/payments/" + res.getPaymentKey()))
                .body(res);
    }


    @GetMapping("/toss/success")
    public RedirectView tossSuccess(@RequestParam Map<String, String> p) {
        log.info("[PAY CALLBACK] success {}", p);

        String pk  = p.get("paymentKey");
        String oid = p.get("orderId");
        Long   amt = p.containsKey("amount") ? Long.valueOf(p.get("amount")) : null;

        if (pk != null && oid != null && amt != null) {
            paymentService.markSuccess(pk, oid, amt);
        } else {
            log.warn("필수 파라미터 누락: {}", p);
        }
        return redirect("/payments/success", p);
    }

    @GetMapping("/toss/fail")
    public RedirectView tossFail(@RequestParam Map<String, String> p) {
        log.info("[PAY CALLBACK] fail {}", p);
        paymentService.markFail(p.get("paymentKey"), p.get("message"));
        return redirect("/payments/fail", p);
    }

    @GetMapping("/{paymentKey}")
    public ResponseEntity<PaymentResDto> getPaymentInfo(@PathVariable String paymentKey) {
        return ResponseEntity.ok(paymentService.getPaymentInfo(paymentKey));
    }

    @PostMapping(path = "/toss/{paymentKey}/refund", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> refundLegacy(@PathVariable String paymentKey,
                                                            @RequestBody Map<String, Object> req) {
        return ResponseEntity.ok(paymentService.refundCardPayment(paymentKey, req));
    }

    @PostMapping(path = "/{paymentKey}/cancel", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PaymentResDto> cancelPayment(@PathVariable String paymentKey,
                                                       @RequestBody @Valid CancelPaymentRequest req) {
        return ResponseEntity.ok(paymentService.refundCardPayment(paymentKey, req));
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleAll(Exception ex) {
        log.error("[PAY][ERROR]", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("status", 500, "message", ex.getMessage()));
    }

    private RedirectView redirect(String path, Map<String, String> params) {
        RedirectView rv = new RedirectView(path, true);   // context-relative
        params.forEach(rv::addStaticAttribute);
        return rv;
    }
}