package com.nhnacademy.bookstoreorderapi.payment.service.impl;

import com.nhnacademy.bookstoreorderapi.order.domain.entity.Order;
import com.nhnacademy.bookstoreorderapi.order.repository.OrderRepository;
import com.nhnacademy.bookstoreorderapi.payment.client.TossPaymentClient;
import com.nhnacademy.bookstoreorderapi.payment.config.TossPaymentConfig;
import com.nhnacademy.bookstoreorderapi.payment.domain.PayType;
import com.nhnacademy.bookstoreorderapi.payment.domain.PaymentStatus;
import com.nhnacademy.bookstoreorderapi.payment.domain.entity.Payment;
import com.nhnacademy.bookstoreorderapi.payment.dto.Request.PaymentReqDto;
import com.nhnacademy.bookstoreorderapi.payment.dto.Response.PaymentResDto;
import com.nhnacademy.bookstoreorderapi.payment.repository.PaymentRepository;
import com.nhnacademy.bookstoreorderapi.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Base64;
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
    private final TossPaymentClient tossClient;

    private String basicAuthHeader() {
        String creds = tossProps.getSecretApiKey() + ":";
        return "Basic " + Base64.getEncoder().encodeToString(creds.getBytes());
    }

    private String extractRedirectUrl(Map<String, Object> r) {
        return Stream.of(
                        r.get("checkoutUrl"),
                        r.get("checkoutPageUrl"),
                        r.get("paymentUrl"),
                        r.get("nextRedirectPcUrl")
                )
                .filter(Objects::nonNull)
                .map(Object::toString)
                .findFirst()
                .orElseThrow(() ->
                        new IllegalStateException("리다이렉트 URL 없음: " + r));
    }

    @Override
    public PaymentResDto requestTossPayment(String orderId, PaymentReqDto dto) {
        // ── 주문 조회 & 중복 결제 방지 ─────────────────────────────
        Order order = orderRepo.findByOrderId(orderId);
        if (order == null) {
            throw new IllegalArgumentException("주문 없음: " + orderId);
        }
        payRepo.findByOrder(order)
                .filter(p -> p.getPaymentStatus() == PaymentStatus.SUCCESS)
                .ifPresent(p -> {
                    throw new IllegalStateException("이미 결제 완료된 주문입니다: " + orderId);
                });

        // ── Toss API 호출용 파라미터 ─────────────────────────────
        String method = dto.getPayType() == PayType.ACCOUNT
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

        // ── Feign Client 호출 & 응답 바디 추출 ────────────────────
        ResponseEntity<Map<String, Object>> createResp =
                tossClient.createPayment(basicAuthHeader(), body);
        Map<String, Object> resp = createResp.getBody();

        if (resp == null || resp.get("paymentKey") == null) {
            throw new IllegalStateException("Toss 결제 생성 응답 오류: " + resp);
        }

        // ── DTO 변환 후 반환 ────────────────────────────────────
        return PaymentResDto.builder()
                .orderId(orderId)
                .payType(dto.getPayType().name())
                .payAmount(dto.getPayAmount())
                .payName(dto.getPayName())
                .paymentKey(resp.get("paymentKey").toString())
                .redirectUrl(extractRedirectUrl(resp))
                .successUrl(tossProps.getSuccessUrl())
                .failUrl(tossProps.getFailUrl())
                .build();
    }

    @Override
    @Transactional
    public void markSuccess(String paymentKey, String orderId, long amount) {
        // ── Toss 확정 API 호출 ────────────────────────────────────
        tossClient.confirmPayment(
                basicAuthHeader(),
                paymentKey,
                Map.of("orderId", orderId, "amount", amount)
        );

        // ── 주문 & 결제 엔티티 upsert ─────────────────────────────
        Order order = orderRepo.findByOrderId(orderId);
        if (order == null) {
            throw new IllegalArgumentException("주문 없음: " + orderId);
        }

        Payment payment = payRepo.findByOrder(order)
                .orElseGet(() -> Payment.builder()
                        .order(order)
                        .payType(PayType.CARD)
                        .payAmount(amount)
                        .payName("도서 구매")
                        .build());

        payment.setPaymentKey(paymentKey);
        payment.setPaymentStatus(PaymentStatus.SUCCESS);
        payment.setPayAmount(amount);
        payRepo.save(payment);
    }

    @Override
    @Transactional
    public void markFail(String paymentKey, String reason) {
        // ── 실패 상태 저장 ───────────────────────────────────────
        payRepo.findByPaymentKey(paymentKey).ifPresent(p -> {
            p.setPaymentStatus(PaymentStatus.FAIL);
            payRepo.save(p);
            log.warn("[PAYMENT-FAIL] {} ▶ {}", paymentKey, reason);
        });
    }

    @Override
    @Transactional
    public Map<String, Object> cancelPaymentPoint(String paymentKey, String reason) {
        // ── 기존 결제 조회 ───────────────────────────────────────
        Payment payment = payRepo.findByPaymentKey(paymentKey)
                .orElseThrow(() -> new IllegalArgumentException("결제 없음: " + paymentKey));

        // ── 취소 API 호출 & 응답 바디 추출 ─────────────────────────
        ResponseEntity<Map<String, Object>> cancelResp =
                tossClient.cancelPayment(basicAuthHeader(), paymentKey, Map.of("cancelReason", reason));
        Map<String, Object> resp = cancelResp.getBody();

        // ── DB 상태 업데이트 ─────────────────────────────────────
        payment.setPaymentStatus(PaymentStatus.CANCEL);
        payRepo.save(payment);

        return resp;
    }
}