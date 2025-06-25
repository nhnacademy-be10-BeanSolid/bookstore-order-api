// src/main/java/com/nhnacademy/bookstoreorderapi/payment/service/impl/PaymentServiceImpl.java
package com.nhnacademy.bookstoreorderapi.payment.service.impl;

import com.nhnacademy.bookstoreorderapi.order.domain.entity.Order;
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
import java.util.Optional;
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

    /* ───────────────────────── 내부 공통 ───────────────────────── */

    /** Toss secret key + JSON 헤더 */
    private HttpHeaders authHeaders() {
        HttpHeaders h = new HttpHeaders();
        h.setBasicAuth(tossProps.getSecretKey(), "");
        h.setContentType(MediaType.APPLICATION_JSON);
        return h;
    }

    /** Toss 응답에서 리다이렉트 URL 한 개를 뽑아낸다 */
    private String extractRedirectUrl(Map<String, Object> resp) {
        // ① 가장 흔한 필드들
        Optional<String> url = Stream.of(
                        resp.get("checkoutUrl"),
                        resp.get("checkoutPageUrl"),
                        resp.get("paymentUrl"),
                        resp.get("nextRedirectPcUrl"),           // 혹시 다른 명칭으로 올 때 대비
                        resp.get("next_redirect_pc_url")
                )
                .filter(Objects::nonNull)
                .map(Object::toString)
                .findFirst();

        if (url.isPresent()) {
            return url.get();
        }

        // ② `checkout` 서브 객체(JSON)로 내려올 때
        Object checkout = resp.get("checkout");
        if (checkout instanceof Map<?,?> map) {
            Object v = map.get("url");          // {"url":"..."} 형태
            if (v == null) v = map.get("pc");   // {"pc":"...","mobile":"..."} 형태
            if (v != null) return v.toString();
        }

        throw new IllegalStateException("리다이렉트 URL이 없습니다: " + resp);
    }

    /* ───────────────────────── 결제 요청 ───────────────────────── */
    @Override
    public PaymentResDto requestTossPayment(String orderId, PaymentReqDto dto) {

        /* 1) 주문 존재 확인 */
        Order order = orderRepo.findByOrderId(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문 없음: " + orderId));

        /* 2) 이미 결제 완료 여부 체크 */
        payRepo.findByOrderId(orderId)
                .filter(Payment::getPaySuccessYn)
                .ifPresent(p -> { throw new IllegalStateException("이미 결제 완료된 주문입니다: " + orderId); });

        /* 3) Toss Payments 결제 생성 */
        String method = (dto.getPayType() == PayType.ACCOUNT)
                ? "VIRTUAL_ACCOUNT"
                : dto.getPayType().name();

        Map<String, Object> body = Map.of(
                "method",     method,
                "orderId",    orderId,
                "orderName",  dto.getPayName(),
                "amount",     dto.getPayAmount(),
                "successUrl", tossProps.getSuccessUrl(),
                "failUrl",    tossProps.getFailUrl()
        );

        Map<String, Object> resp;
        try {
            resp = restTemplate.exchange(
                    TossPaymentConfig.PAYMENTS_URL,
                    HttpMethod.POST,
                    new HttpEntity<>(body, authHeaders()),
                    MAP_REF
            ).getBody();
        } catch (HttpStatusCodeException ex) {
            log.error("[TOSS] 요청 실패 ▶ {} / {}", ex.getStatusCode(), ex.getResponseBodyAsString());
            throw new ResponseStatusException(ex.getStatusCode(), "Toss 결제 요청 실패", ex);
        }

        if (resp == null || resp.get("paymentKey") == null) {
            throw new IllegalStateException("Toss 응답 오류: " + resp);
        }

        /* 4) redirectUrl 추출(수정된 부분) */
        String redirectUrl = extractRedirectUrl(resp);
        String paymentKey  = resp.get("paymentKey").toString();

        /* 5) 프론트에 넘길 DTO 반환 (DB 저장은 성공/실패 콜백에서) */
        return PaymentResDto.builder()
                .orderId    (orderId)
                .payAmount  (dto.getPayAmount())
                .payType    (dto.getPayType().name())
                .payName    (dto.getPayName())
                .paymentKey (paymentKey)
                .redirectUrl(redirectUrl)
                .successUrl (tossProps.getSuccessUrl())
                .failUrl    (tossProps.getFailUrl())
                .build();
    }

    /* ───────────────────────── 결제 성공 콜백 ───────────────────────── */
    @Override
    @Transactional
    public void markSuccess(String paymentKey, String orderId, long amount) {

        /* 1) Toss 결제 확정 – 고정 경로 /confirm */
        Map<String, Object> body = Map.of(
                "paymentKey", paymentKey,
                "orderId",    orderId,
                "amount",     amount
        );

        restTemplate.exchange(
                TossPaymentConfig.PAYMENTS_URL + "/confirm",   // ★ 여기만 바뀜
                HttpMethod.POST,
                new HttpEntity<>(body, authHeaders()),
                Void.class
        );

        /* 2) DB 저장/업데이트 */
        Payment payment = payRepo.findByOrderId(orderId)        // 주문ID로 먼저 조회
                .orElseGet(() -> Payment.builder()
                        .orderId(orderId)
                        .payAmount(amount)
                        .payName("")        // 필요 시 채우기
                        .payType(PayType.CARD)
                        .build());

        payment.setPaymentKey(paymentKey);
        payment.setPaySuccessYn(true);
        payRepo.save(payment);
    }

    /* ───────────────────────── 결제 실패 콜백 ───────────────────────── */
    @Override @Transactional
    public void markFail(String paymentKey, String msg) {
        payRepo.findByPaymentKey(paymentKey).ifPresent(p -> {
            p.setPaySuccessYn(false);
            p.setPayFailReason(msg);
            payRepo.save(p);
        });
    }

    /* ───────────────────────── 결제 취소 ───────────────────────── */
    @Override @Transactional
    public Map<String, Object> cancelPaymentPoint(String paymentKey, String reason) {

        Payment p = payRepo.findByPaymentKey(paymentKey)
                .orElseThrow(() -> new IllegalArgumentException("결제 없음: " + paymentKey));

        Map<String, Object> resp = restTemplate.exchange(
                TossPaymentConfig.PAYMENTS_URL + "/" + paymentKey + "/cancel",
                HttpMethod.POST,
                new HttpEntity<>(Map.of("cancelReason", reason), authHeaders()),
                MAP_REF
        ).getBody();

        p.setPaySuccessYn(false);
        p.setPayFailReason("환불: " + reason);
        payRepo.save(p);

        return resp;
    }
}