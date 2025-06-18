// src/main/java/com/nhnacademy/bookstoreorderapi/payment/controller/PaymentController.java
package com.nhnacademy.bookstoreorderapi.payment.controller;

import com.nhnacademy.bookstoreorderapi.order.domain.entity.Order;
import com.nhnacademy.bookstoreorderapi.order.repository.OrderRepository;
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
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Validated
public class PaymentController {

    private final PaymentService service;
    private final TossPaymentConfig tossProps;
    private final OrderRepository orderRepository;

    /* 1. 결제 요청 */
    @PostMapping(value = "/toss", consumes = "application/json", produces = "application/json")
    public ResponseEntity<PaymentResDto> requestPayment(
            @AuthenticationPrincipal(expression = "username") String email,
            @RequestBody @Valid PaymentReqDto dto) {

//        // 샘플용 안전한 주문번호
//        long orderId = System.currentTimeMillis();

        Order order = orderRepository.findById(1L).orElse(null);

        Payment saved = service.saveInitial(dto.toEntity(order), email);

        String success = (dto.getSuccessUrl() != null && !dto.getSuccessUrl().isBlank())
                ? dto.getSuccessUrl()
                : tossProps.getSuccessUrl();
        String fail = (dto.getFailUrl() != null && !dto.getFailUrl().isBlank())
                ? dto.getFailUrl()
                : tossProps.getFailUrl();

        PaymentResDto res = PaymentResDto.builder()
                .paymentId(saved.getPaymentId())
//                .orderId(saved.getOrderId())
                .orderId(saved.getOrder().getOrderId())
                .payAmount(saved.getPayAmount())
                .payType(saved.getPayType())
                .orderName(saved.getPayName())
                .successUrl(success)
                .failUrl(fail)
                .build();

        return ResponseEntity.ok(res);
    }

    /* 2. 성공 콜백 */
    @GetMapping("/toss/success")
    public ResponseEntity<Void> success(@RequestParam String paymentKey,
                                        @RequestParam String orderId,
                                        @RequestParam Long amount) {

        service.markSuccess(paymentKey, orderId, amount);
        return ResponseEntity.ok().build();
    }

    /* 3. 실패 콜백 */
    @GetMapping("/toss/fail")
    public ResponseEntity<Void> fail(@RequestParam String orderId,
                                     @RequestParam String message) {

        service.markFail(orderId, message);
        return ResponseEntity.ok().build();
    }
}