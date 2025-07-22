package com.nhnacademy.bookstoreorderapi.payment.controller;

import com.nhnacademy.bookstoreorderapi.payment.controller.swagger.PaymentControllerDocs;
import com.nhnacademy.bookstoreorderapi.payment.domain.PayType;
import com.nhnacademy.bookstoreorderapi.payment.dto.Request.PaymentApprovalRequestDto;
import com.nhnacademy.bookstoreorderapi.payment.dto.Request.PaymentReqDto;
import com.nhnacademy.bookstoreorderapi.payment.dto.Response.PaymentResDto;
import com.nhnacademy.bookstoreorderapi.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping(path = "/api/v1/payments", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class PaymentController implements PaymentControllerDocs {

    private static final String PAYMENT_KEY = "paymentKey";

    private final PaymentService paymentService;


    @Value("${frontend.base-url}")
    private String frontBase;


    @PostMapping(path = "/toss/{orderId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PaymentResDto> requestPayment(
            @PathVariable String orderId,
            @Valid @RequestBody PaymentReqDto dto) {

        PaymentResDto res = paymentService.requestTossPayment(orderId, dto);
        return ResponseEntity
                .created(URI.create("/api/v1/payments/" + res.getPaymentKey()))
                .body(res);
    }


    @GetMapping(path = "/toss/{orderId}/create")
    public ResponseEntity<PaymentResDto> requestPaymentViaGet(
            @PathVariable String orderId,
            @RequestParam PayType payType,
            @RequestParam String  payName,
            @RequestParam Long    payAmount) {

        PaymentReqDto dto = new PaymentReqDto();
        dto.setOrderId(orderId);
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
    public ResponseEntity<PaymentApprovalRequestDto> tossSuccess(@RequestParam("paymentKey") String paymentKey,
                                                                 @RequestParam("orderId") String orderId,
                                                                 @RequestParam("amount") long amount){
        PaymentApprovalRequestDto dto = new PaymentApprovalRequestDto(paymentKey, orderId, amount);

        PaymentApprovalRequestDto requestDto = paymentService.markSuccess(dto);
        return  ResponseEntity.ok(requestDto);
    }


    @GetMapping("/fail")
    public RedirectView tossFail(@RequestParam Map<String, String> p) {
        paymentService.markFail(p.get(PAYMENT_KEY), p.get("message"));

        String target = UriComponentsBuilder
                .fromUriString(frontBase + "/payments/fail")
                .queryParam(PAYMENT_KEY, p.get(PAYMENT_KEY))
                .queryParam("orderId",    p.get("orderId"))
                .build()
                .toUriString();

        return new RedirectView(target, false);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleAll(Exception ex) {
        log.error("[PAY][ERROR]", ex);
        return ResponseEntity
                .status(500)
                .body(Map.of("status", 500, "message", ex.getMessage()));
    }
}