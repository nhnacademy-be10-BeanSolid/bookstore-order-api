package com.nhnacademy.bookstoreorderapi.payment.controller;

import com.nhnacademy.bookstoreorderapi.payment.domain.PayType;
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

    /**
     * (1) JSON 바디로 Toss 결제 요청
     * POST /api/v1/payments/toss/{orderId}
     */
    @CrossOrigin(origins = "*")
    @PostMapping(path = "/toss/{orderId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PaymentResDto> requestPayment(
            @PathVariable("orderId") String orderId,
            @RequestBody @Valid PaymentReqDto dto) {

        PaymentResDto res = paymentService.requestTossPayment(orderId, dto);
        return ResponseEntity
                .created(URI.create("/api/v1/payments/" + res.getPaymentKey()))
                .body(res);
    }

    /**
     * (2) GET 방식으로 Toss 결제 요청 (query parameter)
     * GET /api/v1/payments/toss/{orderId}/create
     */
    @CrossOrigin(origins = "*")
    @GetMapping(path = "/toss/{orderId}/create")
    public ResponseEntity<PaymentResDto> requestPaymentViaGet(
            @PathVariable("orderId") String orderId,
            @RequestParam("payType")  PayType payType,
            @RequestParam("payName")  String  payName,
            @RequestParam("payAmount") Long    payAmount) {

        PaymentReqDto dto = new PaymentReqDto();
        dto.setPayType(payType);
        dto.setPayName(payName);
        dto.setPayAmount(payAmount);

        PaymentResDto res = paymentService.requestTossPayment(orderId, dto);
        return ResponseEntity
                .created(URI.create("/api/v1/payments/" + res.getPaymentKey()))
                .body(res);
    }

    /**
     * (3) NEW! 결제 정보 조회
     * GET /api/v1/payments/{paymentKey}
     */
    @GetMapping(path = "/{paymentKey}")
    public ResponseEntity<PaymentResDto> getPaymentInfo(
            @PathVariable("paymentKey") String paymentKey) {

        PaymentResDto info = paymentService.getPaymentInfo(paymentKey);
        return ResponseEntity.ok(info);
    }

    /**
     * (4) Toss 성공 콜백
     * GET /api/v1/payments/toss/success
     */
    @GetMapping("/toss/success")
    public RedirectView tossSuccess(@RequestParam Map<String,String> params) {
        log.info("[PAY CALLBACK] success: params={}", params);
        String pk  = params.get("paymentKey");
        String oid = params.get("orderId");
        Long   amt = params.containsKey("amount")
                ? Long.valueOf(params.get("amount")) : null;

        if (pk != null && oid != null && amt != null) {
            paymentService.markSuccess(pk, oid, amt);
        } else {
            log.warn("파라미터 누락: {}", params);
        }

        RedirectView rv = new RedirectView("/success.html");
        params.forEach(rv::addStaticAttribute);
        return rv;
    }

    /**
     * (5) Toss 실패 콜백
     * GET /api/v1/payments/toss/fail
     */
    @GetMapping("/toss/fail")
    public RedirectView tossFail(@RequestParam Map<String,String> params) {
        log.info("[PAY CALLBACK] fail: params={}", params);
        paymentService.markFail(params.get("paymentKey"), params.get("message"));

        RedirectView rv = new RedirectView("/fail.html");
        params.forEach(rv::addStaticAttribute);
        return rv;
    }

    /**
     * (6) 포인트 결제 취소(환불)
     * POST /api/v1/payments/toss/{paymentKey}/cancel
     */
    @CrossOrigin(origins = "*")
    @PostMapping(path = "/toss/{paymentKey}/cancel",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<Map<String,Object>> cancelAndRefund(
            @PathVariable("paymentKey") String paymentKey,
            @RequestParam("cancelReason") String cancelReason) {

        log.info("[PAY CANCEL] paymentKey={} reason={}", paymentKey, cancelReason);
        Map<String,Object> resp = paymentService.cancelPaymentPoint(paymentKey, cancelReason);
        return ResponseEntity.ok(resp);
    }

    /**
     * (7) 대체 Cancel 엔드포인트 (포인트 전용)
     * POST /api/v1/payments/toss/cancel/point
     */
    @PostMapping(path = "/toss/cancel/point",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<Map<String,Object>> cancelPaymentPoint(
            @RequestParam String paymentKey,
            @RequestParam String cancelReason) {

        return ResponseEntity.ok(
                paymentService.cancelPaymentPoint(paymentKey, cancelReason)
        );
    }

    /**
     * 모든 예외를 잡아서 500 응답으로 변환
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String,Object>> handleAll(Exception ex) {
        log.error("[PAY][ERROR]", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                        "status",  HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "message", ex.getMessage()
                ));
    }
}