package com.nhnacademy.bookstoreorderapi.payment.service.impl;

import com.nhnacademy.bookstoreorderapi.payment.config.TossPaymentConfig;
import com.nhnacademy.bookstoreorderapi.payment.domain.entity.Payment;
import com.nhnacademy.bookstoreorderapi.payment.repository.PaymentRepository;
import com.nhnacademy.bookstoreorderapi.payment.service.PaymentService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Service
@Transactional
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepo;
    private final TossPaymentConfig tossProps;

    /** 1) 최초 DB 저장 */
    @Override
    public Payment saveInitial(Payment payment, String userEmail) {
        if (payment.getPayAmount() < 1_000) {
            throw new IllegalArgumentException("최소 결제금액은 1,000원입니다.");
        }
        // userEmail → 필요한 경우 payment.setUserId(…) 등
        return paymentRepo.save(payment);
    }

    /** 2) 결제 성공 */
    @Override
    public void markSuccess(String paymentKey, Long orderId, long amount) {

        Payment payment = paymentRepo.findByOrderId(orderId)
                .orElseThrow(() -> new IllegalStateException("주문 ID 없음"));

        // 토스 결제 승인
        RestTemplate rt = new RestTemplate();
        HttpHeaders headers = authHeaders(tossProps.getTestSecretApiKey());
        HttpEntity<Object> body = new HttpEntity<>(
                new TossAcceptBody(orderId, amount), headers);

        rt.postForObject(
                TossPaymentConfig.PAYMENTS_URL + paymentKey,
                body,
                Object.class);

        payment.setPaySuccessYn(true);
        payment.setPaymentKey(paymentKey);
    }

    /** 3) 결제 실패 */
    @Override
    public void markFail(Long orderId, String failMessage) {
        paymentRepo.findByOrderId(orderId)
                .ifPresent(p -> {
                    p.setPaySuccessYn(false);
                    p.setPayFailReason(failMessage);
                });
    }

    /* ───────── private util ───────── */
    private HttpHeaders authHeaders(String secretKey) {
        String enc = Base64.getEncoder()
                .encodeToString((secretKey + ":").getBytes(StandardCharsets.UTF_8));
        HttpHeaders h = new HttpHeaders();
        h.setBasicAuth(enc);
        h.setContentType(MediaType.APPLICATION_JSON);
        return h;
    }

    /** 토스 승인 요청 바디 */
    private record TossAcceptBody(Long orderId, Long amount) {}
}