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


    @CrossOrigin(origins = "*")
    @PostMapping(path = "/toss/{paymentKey}/cancel",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<Map<String,Object>> cancelAndRefund(
            @PathVariable String paymentKey,
            @RequestParam("cancelReason") String cancelReason) {

        log.info("[PAY CANCEL] paymentKey={} reason={}", paymentKey, cancelReason);
        Map<String,Object> resp = paymentService.cancelPaymentPoint(paymentKey, cancelReason);
        return ResponseEntity.ok(resp);
    }



    @CrossOrigin(origins = "*")
    @GetMapping(path = "/toss/{orderId}/create")
    public ResponseEntity<PaymentResDto> requestPaymentViaGet(
            @PathVariable String orderId,
            @RequestParam("payType")  PayType payType,
            @RequestParam("payName")  String  payName,
            @RequestParam("payAmount") Long    payAmount) {

        PaymentReqDto dto = new PaymentReqDto();
        dto.setPayType(payType);
        dto.setPayName(payName);
        dto.setPayAmount(payAmount);

        PaymentResDto res = paymentService.requestTossPayment(orderId, dto);

        return ResponseEntity
                .created(URI.create("/api/v1/payments/" + res.getPaymentId()))
                .body(res);
    }



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



    @GetMapping("/toss/fail")
    public RedirectView tossFail(@RequestParam Map<String,String> params) {
        log.info("[PAY CALLBACK] fail: params={}", params);
        paymentService.markFail(params.get("paymentKey"), params.get("message"));

        RedirectView rv = new RedirectView("/fail.html");
        params.forEach(rv::addStaticAttribute);
        return rv;
    }



    @PostMapping(path = "/toss/cancel/point",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<Map<String,Object>> cancelPaymentPoint(
            @RequestParam String paymentKey,
            @RequestParam String cancelReason) {

        return ResponseEntity.ok(
                paymentService.cancelPaymentPoint(paymentKey, cancelReason)
        );
    }


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