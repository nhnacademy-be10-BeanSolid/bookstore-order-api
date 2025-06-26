package com.nhnacademy.bookstoreorderapi.payment.service.impl;

import com.nhnacademy.bookstoreorderapi.order.repository.OrderRepository;
import com.nhnacademy.bookstoreorderapi.payment.config.TossPaymentConfig;
import com.nhnacademy.bookstoreorderapi.payment.domain.PayType;
import com.nhnacademy.bookstoreorderapi.payment.domain.entity.Payment;
import com.nhnacademy.bookstoreorderapi.payment.dto.Request.PaymentReqDto;
import com.nhnacademy.bookstoreorderapi.payment.dto.Response.PaymentResDto;
import com.nhnacademy.bookstoreorderapi.payment.repository.PaymentRepository;
import com.nhnacademy.bookstoreorderapi.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final OrderRepository   orderRepo;
    private final PaymentRepository payRepo;
    private final TossPaymentConfig tossProps;
    private final RestTemplate      restTemplate;

    private static final ParameterizedTypeReference<Map<String, Object>> MAP_REF =
            new ParameterizedTypeReference<>() {};

    /* ---------- 공통 헤더 ---------- */
    private HttpHeaders authHeaders() {
        HttpHeaders h = new HttpHeaders();
        h.setBasicAuth(tossProps.getSecretKey(), "");
        h.setContentType(MediaType.APPLICATION_JSON);
        return h;
    }

    /* ---------- redirect URL 추출 ---------- */
    private String extractRedirectUrl(Map<String, Object> resp) {
        return Stream.of(
                        resp.get("checkoutUrl"),
                        resp.get("checkoutPageUrl"),
                        resp.get("paymentUrl"),
                        resp.get("nextRedirectPcUrl"),
                        resp.get("next_redirect_pc_url"))
                .filter(Objects::nonNull)
                .map(Object::toString)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("리다이렉트 URL이 없습니다: " + resp));
    }

    // 1. 결제 생성 요청
    @Override
    public PaymentResDto requestTossPayment(String orderId, PaymentReqDto dto) {

        // 1) 주문 존재 확인
        if (!orderRepo.existsByOrderId(orderId)) {
            throw new IllegalArgumentException("주문 없음: " + orderId);
        }

        // 2) 이미 성공 결제가 있는지 확인 (실패건은 허용)
        payRepo.findByOrderId(orderId)
                .filter(p -> p.getPaymentStatus() == null)
                .ifPresent(p -> { throw new IllegalStateException("이미 결제 완료된 주문입니다: " + orderId); });

        // 3) Toss 결제 생성
        String method = dto.getPayType() == PayType.ACCOUNT ? "VIRTUAL_ACCOUNT"
                : dto.getPayType().name();

        Map<String, Object> body = Map.of(
                "method",     method,
                "orderId",    orderId,
                "orderName",  dto.getPayName(),
                "amount",     dto.getPayAmount(),
                "successUrl", tossProps.getSuccessUrl(),
                "failUrl",    tossProps.getFailUrl());

        Map<String, Object> resp;
        try {
            resp = restTemplate.exchange(
                    TossPaymentConfig.PAYMENTS_URL,
                    HttpMethod.POST,
                    new HttpEntity<>(body, authHeaders()),
                    MAP_REF).getBody();
        } catch (HttpStatusCodeException ex) {
            log.error("[TOSS] 요청 실패 ▶ {} / {}", ex.getStatusCode(), ex.getResponseBodyAsString());
            throw new ResponseStatusException(ex.getStatusCode(), "Toss 결제 요청 실패", ex);
        }

        if (resp == null || resp.get("paymentKey") == null) {
            throw new IllegalStateException("Toss 응답 오류: " + resp);
        }

        String redirectUrl = extractRedirectUrl(resp);
        String paymentKey  = resp.get("paymentKey").toString();

        return PaymentResDto.builder()
                .orderId     (orderId)
                .payAmount   (dto.getPayAmount())
                .payType     (dto.getPayType().name())
                .payName     (dto.getPayName())
                .paymentKey  (paymentKey)
                .redirectUrl (redirectUrl)
                .successUrl  (tossProps.getSuccessUrl())
                .failUrl     (tossProps.getFailUrl())
                .build();
    }

    //2. 결제 성공 콜백
    @Override
    @Transactional
    public void markSuccess(String paymentKey, String orderId, long amount) {

        // Toss 확정
        restTemplate.exchange(
                TossPaymentConfig.PAYMENTS_URL + "/" + paymentKey + "/confirm",
                HttpMethod.POST,
                new HttpEntity<>(Map.of("orderId", orderId, "amount", amount), authHeaders()),
                Void.class);

        // DB 저장 / 업데이트
        Payment payment = payRepo.findByOrderId(orderId)
                .orElseGet(() -> Payment.builder()
                        .orderId   (orderId)
                        .payAmount (amount)
                        .payName   ("도서 구매")
                        .payType   (PayType.CARD)
                        .build());

        payment.setPaymentKey(payment.getPaymentKey());   // String 그대로
        payment.setPaymentStatus(null);      // 성공 처리
        payRepo.save(payment);
    }

    // 3. 결제 실패 콜백
    @Override
    @Transactional
    public void markFail(String paymentKey, String msg) {
        payRepo.findByPaymentKey(paymentKey).ifPresent(p -> {
            p.setPaymentStatus(msg);
            payRepo.save(p);
        });
    }

    // 4. 결제 취소
    @Override
    @Transactional
    public Map<String, Object> cancelPaymentPoint(String paymentKey, String reason) {

        Payment p = payRepo.findByPaymentKey(paymentKey)
                .orElseThrow(() -> new IllegalArgumentException("결제 없음: " + paymentKey));

        Map<String, Object> resp = restTemplate.exchange(
                TossPaymentConfig.PAYMENTS_URL + "/" + paymentKey + "/cancel",
                HttpMethod.POST,
                new HttpEntity<>(Map.of("cancelReason", reason), authHeaders()),
                MAP_REF).getBody();

        p.setPaymentStatus("환불: " + reason);
        payRepo.save(p);

        return resp;
    }
}