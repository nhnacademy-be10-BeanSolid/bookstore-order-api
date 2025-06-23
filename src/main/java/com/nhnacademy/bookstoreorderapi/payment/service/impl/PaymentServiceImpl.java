package com.nhnacademy.bookstoreorderapi.payment.service.impl;

import com.nhnacademy.bookstoreorderapi.payment.config.TossPaymentConfig;
import com.nhnacademy.bookstoreorderapi.payment.domain.entity.Payment;
import com.nhnacademy.bookstoreorderapi.payment.repository.PaymentRepository;
import com.nhnacademy.bookstoreorderapi.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@Transactional
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {
    private final PaymentRepository paymentRepo;
    private final TossPaymentConfig tossProps;
    private final RestTemplate restTemplate;

    @Override
    public Payment saveInitial(Payment payment, String userId) {
        if (payment.getPayAmount() == null) {
            throw new IllegalArgumentException("결제 금액을 입력해주세요.");
        }
        if (payment.getPayAmount() < 1_000) {
            throw new IllegalArgumentException("최소 결제금액은 1,000원입니다.");
        }
        payment.setPaySuccessYn(false);
        payment.setPaymentKey(null);
        return paymentRepo.save(payment);
    }

    @Override
    public void markSuccess(String paymentKey, String orderId, long amount) {
        Payment p = paymentRepo.findByPaymentKey(paymentKey)
                .orElseThrow(() -> new IllegalArgumentException("해당 결제 내역이 없습니다: " + paymentKey));

        if (!p.getOrder().getOrderId().equals(orderId)) {
            throw new IllegalArgumentException("잘못된 주문번호입니다: " + orderId);
        }
        if (!p.getPayAmount().equals(amount)) {
            throw new IllegalArgumentException(
                    "잘못된 결제금액입니다: 예상=" + p.getPayAmount() + ", 실제=" + amount);
        }
        if (Boolean.TRUE.equals(p.getPaySuccessYn())) {
            return;
        }

        if (!tossProps.isSandbox()) {
            HttpHeaders headers = new HttpHeaders();
            headers.setBasicAuth(tossProps.getTestSecretApiKey(), "");
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> req = new HttpEntity<>(
                    Map.of("orderId", orderId, "amount", amount),
                    headers
            );

            restTemplate.postForEntity(
                    TossPaymentConfig.PAYMENTS_URL + paymentKey + "/confirm",
                    req,
                    Void.class
            );
        }

        p.setPaySuccessYn(true);
        p.setPaymentKey(paymentKey);
        paymentRepo.save(p);
    }

    @Override
    public void markFail(String paymentKey, String failMessage) {
        paymentRepo.findByPaymentKey(paymentKey)
                .ifPresent(p -> {
                    p.setPaySuccessYn(false);
                    p.setPayFailReason(failMessage);
                    paymentRepo.save(p);
                });
    }

    @Override
    public Map<String, Object> cancelPaymentPoint(
            String paymentKey, String cancelReason, String userId, Long guestId) {

        Payment payment = paymentRepo.findByPaymentKey(paymentKey)
                .orElseThrow(() -> new IllegalArgumentException("해당 결제가 없습니다: " + paymentKey));

        Map<String, Object> response;
        if (tossProps.isSandbox()) {
            response = Map.of(
                    "status",       "CANCELED",
                    "requestedAt",  System.currentTimeMillis(),
                    "cancelReason", cancelReason
            );
        } else {
            HttpHeaders headers = new HttpHeaders();
            headers.setBasicAuth(tossProps.getTestSecretApiKey(), "");
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, String>> req =
                    new HttpEntity<>(Map.of("cancelReason", cancelReason), headers);

            response = restTemplate.postForObject(
                    TossPaymentConfig.PAYMENTS_URL + paymentKey + "/cancel",
                    req,
                    Map.class
            );
        }

        payment.setPaySuccessYn(false);
        payment.setPayFailReason("환불: " + cancelReason);
        paymentRepo.save(payment);

        return response;
    }
}