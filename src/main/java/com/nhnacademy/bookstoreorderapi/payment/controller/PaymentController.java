package com.nhnacademy.bookstoreorderapi.payment.controller;

import com.nhnacademy.bookstoreorderapi.order.domain.entity.Order;
import com.nhnacademy.bookstoreorderapi.order.repository.OrderRepository;
import com.nhnacademy.bookstoreorderapi.payment.config.TossPaymentConfig;
import com.nhnacademy.bookstoreorderapi.payment.domain.entity.Payment;
import com.nhnacademy.bookstoreorderapi.payment.dto.Request.PaymentReqDto;
import com.nhnacademy.bookstoreorderapi.payment.dto.Response.PaymentResDto;
import com.nhnacademy.bookstoreorderapi.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping(path = "/api/v1/payments", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService    service;
    @Qualifier("tossPaymentConfig")
    private final TossPaymentConfig tossProps;
    private final OrderRepository   orderRepository;
    private final RestTemplate      restTemplate;

    /**
     * 1) 결제 요청: POST /api/v1/payments/toss/{orderId}
     */
    @PostMapping(path = "/toss/{orderId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PaymentResDto> requestPayment(
            @AuthenticationPrincipal(expression = "username") String userId,
            @PathVariable String orderId,
            @RequestBody @Valid PaymentReqDto dto) {

        log.info("[PAY] 진입: userId={} orderId={} dto={}", userId, orderId, dto);

        Order order = orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new IllegalArgumentException("해당 주문이 없습니다: " + orderId));

        Payment saved = service.saveInitial(dto.toEntity(order), userId);

        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(tossProps.getTestClientApiKey(), "");
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String,Object> body = Map.of(
                "amount",     saved.getPayAmount(),
                "orderId",    saved.getOrder().getOrderId(),
                "orderName",  saved.getPayName(),
                "successUrl", StringUtils.hasText(dto.getSuccessUrl())
                        ? dto.getSuccessUrl() : tossProps.getSuccessUrl(),
                "failUrl",    StringUtils.hasText(dto.getFailUrl())
                        ? dto.getFailUrl()    : tossProps.getFailUrl()
        );
        log.info("[PAY] Toss 요청 준비: url={} body={}", TossPaymentConfig.PAYMENTS_URL, body);

        HttpEntity<Map<String,Object>> req = new HttpEntity<>(body, headers);

        Map<String,Object> resp;
        try {
            @SuppressWarnings("unchecked")
            Map<String,Object> tmp = restTemplate.postForObject(
                    TossPaymentConfig.PAYMENTS_URL,
                    req,
                    Map.class
            );
            resp = tmp;
            log.info("[PAY] Toss 응답: {}", resp);
        } catch (HttpClientErrorException e) {
            log.error("[PAY][ERROR] HTTP error: status={} body={}",
                    e.getStatusCode(), e.getResponseBodyAsString(), e);
            throw e;
        } catch (ResourceAccessException e) {
            log.error("[PAY][ERROR] Connection error", e);
            throw e;
        }

        PaymentResDto result = PaymentResDto.builder()
                .paymentKey((String) resp.get("paymentKey"))
                .nextRedirectAppUrl((String) resp.get("nextRedirectAppUrl"))
                .nextRedirectPcUrl((String) resp.get("nextRedirectPcUrl"))
                .orderId(saved.getOrder().getOrderId())
                .payAmount(saved.getPayAmount())
                .payType(saved.getPayType().name())
                .orderName(saved.getPayName())
                .successUrl(body.get("successUrl").toString())
                .failUrl(body.get("failUrl").toString())
                .build();

        log.info("[PAY] 응답 DTO 생성 완료, returning 201");
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    /**
     * 2) 결제 성공 콜백: GET /api/v1/payments/toss/success
     */
    @GetMapping(path = "/toss/success")
    public ResponseEntity<Void> successCallback(
            @RequestParam String paymentKey,
            @RequestParam String orderId,
            @RequestParam Long amount) {

        log.info("[PAY CALLBACK] 성공 콜백: paymentKey={} orderId={} amount={}",
                paymentKey, orderId, amount);

        service.markSuccess(paymentKey, orderId, amount);

        URI redirectUri = URI.create(String.format(
                "/success.html?paymentKey=%s&orderId=%s&amount=%d",
                paymentKey, orderId, amount
        ));
        log.info("[PAY CALLBACK] 리다이렉트: {}", redirectUri);
        return ResponseEntity.status(HttpStatus.FOUND).location(redirectUri).build();
    }

    /**
     * 3) 결제 실패 콜백: GET /api/v1/payments/toss/fail
     */
    @GetMapping(path = "/toss/fail")
    public ResponseEntity<Void> failCallback(
            @RequestParam String paymentKey,
            @RequestParam String orderId,
            @RequestParam String message) {

        log.info("[PAY CALLBACK] 실패 콜백: paymentKey={} orderId={} message={}",
                paymentKey, orderId, message);

        service.markFail(paymentKey, message);

        URI redirectUri = URI.create(String.format(
                "/fail.html?paymentKey=%s&orderId=%s&message=%s",
                paymentKey, orderId, message
        ));
        log.info("[PAY CALLBACK] 리다이렉트: {}", redirectUri);
        return ResponseEntity.status(HttpStatus.FOUND).location(redirectUri).build();
    }

    /**
     * 4) 포인트 환불(취소): POST /api/v1/payments/toss/cancel/point
     */
    @PostMapping(path = "/toss/cancel/point", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<Map<String, Object>> cancelPaymentPoint(
            @AuthenticationPrincipal(expression = "username") String userId,
            @RequestParam String paymentKey,
            @RequestParam String cancelReason,
            @RequestParam(required = false) Long guestId) {

        log.info("[PAY CANCEL] 환불 요청: paymentKey={} cancelReason={}", paymentKey, cancelReason);

        Map<String, Object> result = service.cancelPaymentPoint(
                paymentKey, cancelReason, userId, guestId);

        log.info("[PAY CANCEL] 환불 응답: {}", result);
        return ResponseEntity.ok(result);
    }

}