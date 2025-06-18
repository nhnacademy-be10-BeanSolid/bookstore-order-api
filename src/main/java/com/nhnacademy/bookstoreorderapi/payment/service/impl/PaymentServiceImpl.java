package com.nhnacademy.bookstoreorderapi.payment.service.impl;

import com.nhnacademy.bookstoreorderapi.payment.config.TossPaymentConfig;
import com.nhnacademy.bookstoreorderapi.payment.domain.entity.Payment;
import com.nhnacademy.bookstoreorderapi.payment.repository.PaymentRepository;
import com.nhnacademy.bookstoreorderapi.payment.service.PaymentService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
@Transactional
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepo;
    private final TossPaymentConfig    tossProps;
    private final RestTemplate         restTemplate;  // @Bean 으로 등록된 RestTemplate

    @Override
    public Payment saveInitial(Payment payment, String userEmail) {
        if (payment.getPayAmount() < 1_000) {
            throw new IllegalArgumentException("최소 결제금액은 1,000원입니다.");
        }
        return paymentRepo.save(payment);
    }

    @Override
    public void markSuccess(String paymentKey, Long orderId, long amount) {
        Payment pay = paymentRepo.findByOrderId(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문 ID 없음"));

        if (!pay.getPayAmount().equals(amount)) {
            throw new IllegalArgumentException("요청 금액 불일치");
        }
        // 이미 성공 처리된 건은 무시
        if (Boolean.TRUE.equals(pay.getPaySuccessYn())) {
            return;
        }

        // 실제 토스 승인 호출 (sandbox 모드가 아니면)
        if (!tossProps.isSandbox()) {
            HttpHeaders headers = new HttpHeaders();
            headers.setBasicAuth(tossProps.getTestSecretApiKey(), "");
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<TossAcceptBody> request = new HttpEntity<>(
                    new TossAcceptBody(orderId, amount),
                    headers
            );
            try {
                restTemplate.postForEntity(
                        // 반드시 /confirm 을 붙여야 승인 API 호출됩니다
                        TossPaymentConfig.PAYMENTS_URL + paymentKey + "/confirm",
                        request,
                        Void.class
                );
            } catch (RestClientException ex) {
                markFail(orderId, "토스 승인 실패: " + ex.getMessage());
                throw new IllegalStateException("PG 승인 실패", ex);
            }
        }

        // DB 업데이트
        pay.setPaySuccessYn(true);
        pay.setPaymentKey(paymentKey);
        paymentRepo.save(pay);
    }

    @Override
    public void markFail(Long orderId, String failMessage) {
        paymentRepo.findByOrderId(orderId)
                .ifPresent(p -> {
                    p.setPaySuccessYn(false);
                    p.setPayFailReason(failMessage);
                    paymentRepo.save(p);
                });
    }

    /** 토스 승인 요청 바디용 내부 클래스 */
    private record TossAcceptBody(Long orderId, Long amount) {}
}