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
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final OrderRepository   orderRepo;
    private final PaymentRepository payRepo;
    private final TossPaymentConfig tossProps;
    private final RestTemplate      restTemplate;

    /* ─────────────────────────  공통 헤더 ───────────────────────── */
    private HttpHeaders authHeaders() {
        HttpHeaders h = new HttpHeaders();
        h.setBasicAuth(tossProps.getSecretKey(), "");
        h.setContentType(MediaType.APPLICATION_JSON);
        return h;
    }
    private static final ParameterizedTypeReference<Map<String, Object>> MAP_REF =
            new ParameterizedTypeReference<>() {};

    /* ───────────────── 1) 결제창(인증) 생성 ───────────────── */
    @Override
    @Transactional(
            propagation   = Propagation.REQUIRES_NEW,
            noRollbackFor = ResponseStatusException.class
    )
    public PaymentResDto requestTossPayment(String orderId, PaymentReqDto dto) {

        /* ① 주문 확인 */
        Order order = orderRepo.findByOrderId(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문 없음: " + orderId));

        /* ② 미완료 결제 재사용 / 신규 */
        Payment payment = payRepo.findByOrderId(orderId)
                .filter(p -> !Boolean.TRUE.equals(p.getPaySuccessYn()))
                .orElseGet(() -> {
                    Payment p = dto.toEntity(order);
                    p.setPaySuccessYn(false);
                    return payRepo.saveAndFlush(p);          // 즉시 INSERT
                });

        log.info("✅ Payment INSERT = {}", payment);          // ★ ① INSERT 확인용

        /* ③ Toss 요청 데이터 */
        String successUrl = tossProps.getSuccessUrl() + "?orderId=" + orderId;
        String failUrl    = tossProps.getFailUrl()    + "?orderId=" + orderId;
        String method     = payment.getPayType() == PayType.ACCOUNT
                ? "VIRTUAL_ACCOUNT" : payment.getPayType().name();

        Map<String,Object> body = Map.of(
                "method",     method,
                "orderId",    orderId,
                "orderName",  payment.getPayName(),
                "amount",     payment.getPayAmount(),
                "successUrl", successUrl,
                "failUrl",    failUrl
        );

        Map<String,Object> resp;
        try {
            resp = restTemplate.exchange(
                    TossPaymentConfig.PAYMENTS_URL,
                    HttpMethod.POST,
                    new HttpEntity<>(body, authHeaders()),
                    MAP_REF
            ).getBody();
        } catch (HttpStatusCodeException ex) {
            log.error("[TOSS] 요청 실패 ▶ {} / {}", ex.getStatusCode(), ex.getResponseBodyAsString());
            payment.setPayFailReason(ex.getStatusText());
            payRepo.save(payment);
            throw new ResponseStatusException(ex.getStatusCode(), "Toss 결제 실패", ex);
        }

        if (resp == null || resp.get("paymentKey") == null)
            throw new IllegalStateException("Toss 응답 오류: " + resp);

        payment.setPaymentKey(resp.get("paymentKey").toString());
        payRepo.save(payment);

        /* ★ ② commit 완료 시점 확인 */
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override public void afterCommit() {
                        log.info("🎉 Payment TX committed (id={})", payment.getPaymentId());
                    }
                });

        /* ④ 결과 DTO */
        return PaymentResDto.builder()
                .paymentId          (payment.getPaymentId())
                .orderId            (payment.getOrderId())
                .payAmount          (payment.getPayAmount())
                .payType            (payment.getPayType().name())
                .payName            (payment.getPayName())
                .paymentKey         (payment.getPaymentKey())
                .successUrl         (successUrl)
                .failUrl            (failUrl)
                .build();
    }

    /* ───────────────── 2) 결제 확정 콜백 ───────────────── */
    @Override @Transactional
    public void markSuccess(String paymentKey, String orderId, long amount) {
        Payment p = payRepo.findByPaymentKey(paymentKey)
                .orElseThrow(() -> new IllegalArgumentException("결제 없음: " + paymentKey));

        if (!p.getOrderId().equals(orderId) || !p.getPayAmount().equals(amount))
            throw new IllegalArgumentException("결제 정보 불일치");
        if (Boolean.TRUE.equals(p.getPaySuccessYn())) return;

        try {
            restTemplate.exchange(
                    TossPaymentConfig.PAYMENTS_URL + "/" + paymentKey + "/confirm",
                    HttpMethod.POST,
                    new HttpEntity<>(Map.of("orderId", orderId, "amount", amount), authHeaders()),
                    Void.class
            );
        } catch (HttpStatusCodeException ex) {
            log.error("[TOSS] 확정 실패 ▶ {} / {}", ex.getStatusCode(), ex.getResponseBodyAsString());
            throw new ResponseStatusException(ex.getStatusCode(), "Toss 결제 확정 실패", ex);
        }

        p.setPaySuccessYn(true);
        payRepo.save(p);
    }

    /* ───── 3) 결제 실패 콜백 ───── */
    @Override @Transactional
    public void markFail(String paymentKey, String msg) {
        payRepo.findByPaymentKey(paymentKey).ifPresent(p -> {
            p.setPaySuccessYn(false);
            p.setPayFailReason(msg);
            payRepo.save(p);
        });
    }

    /* ───── 4) 포인트 환불 ───── */
    @Override @Transactional
    public Map<String,Object> cancelPaymentPoint(String paymentKey, String reason) {
        Payment p = payRepo.findByPaymentKey(paymentKey)
                .orElseThrow(() -> new IllegalArgumentException("결제 없음: " + paymentKey));

        Map<String,Object> resp;
        try {
            resp = restTemplate.exchange(
                    TossPaymentConfig.PAYMENTS_URL + "/" + paymentKey + "/cancel",
                    HttpMethod.POST,
                    new HttpEntity<>(Map.of("cancelReason", reason), authHeaders()),
                    MAP_REF
            ).getBody();
        } catch (HttpStatusCodeException ex) {
            log.error("[TOSS] 취소 실패 ▶ {} / {}", ex.getStatusCode(), ex.getResponseBodyAsString());
            throw new ResponseStatusException(ex.getStatusCode(), "Toss 결제 취소 실패", ex);
        }
        if (resp == null) throw new IllegalStateException("Toss 환불 응답 없음");

        p.setPaySuccessYn(false);
        p.setPayFailReason("환불: " + reason);
        payRepo.save(p);
        return resp;
    }
}