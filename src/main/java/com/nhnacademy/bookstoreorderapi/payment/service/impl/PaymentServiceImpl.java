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
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements com.nhnacademy.bookstoreorderapi.payment.service.PaymentService {

    private final OrderRepository orderRepo;
    private final PaymentRepository payRepo;
    private final TossPaymentConfig tossProps;
    private final TossPaymentClient tossClient;

    /**
     * 응답 맵에서 redirect URL을 추출.
     * ① checkout.url 우선 확인
     * ② 그 외 키 순차 탐색
     */
    private String extractRedirectUrl(Map<String, Object> resp) {
        Object checkout = resp.get("checkout");
        if (checkout instanceof Map<?, ?> nested) {
            Object url = nested.get("url");
            if (url != null) {
                return url.toString();
            }
        }
        return Stream.of(
                        resp.get("checkoutUrl"),
                        resp.get("checkoutPageUrl"),
                        resp.get("paymentUrl"),
                        resp.get("nextRedirectPcUrl") //결제 페이지로 바로 이동할수 있는 URL이 담겨있음
                )
                .filter(Objects::nonNull)
                .map(Object::toString)
                .findFirst()
                .orElseThrow(() ->
                        new IllegalStateException("리다이렉트 URL 없음: " + resp)
                );
    }

    @Override
    public PaymentResDto requestTossPayment(String orderId, PaymentReqDto dto) {
        Order order = orderRepo.findByOrderId(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문 없음: " + orderId));
        payRepo.findByOrder(order)
                .filter(p -> p.getPaymentStatus() == PaymentStatus.SUCCESS)
                .ifPresent(p -> {
                    throw new IllegalStateException("이미 결제 완료된 주문입니다: " + orderId);
                });

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

        Map<String, Object> resp = tossClient.createPayment(body);

        Object key = resp.get("paymentKey");
        if (key == null) {
            log.error("[TOSS CREATE][ERROR] no paymentKey in {}", resp);
            throw new IllegalStateException("Toss 생성 오류: " + resp);
        }

        return PaymentResDto.builder()
                .orderId(orderId)
                .payType(dto.getPayType().name())
                .payAmount(dto.getPayAmount())
                .payName(dto.getPayName())
                .paymentKey(key.toString())
                .redirectUrl(extractRedirectUrl(resp))
                .successUrl(tossProps.getSuccessUrl())
                .failUrl(tossProps.getFailUrl())
                .build();
    }

    @Override
    @Transactional
    public void markSuccess(String paymentKey, String orderId, long amount) {
        try {
            // CARD 결제의 경우 confirmPayment 엔드포인트가 없으므로,
            // 404(Not Found)만 무시하고 넘어갑니다.
            tossClient.confirmPayment(paymentKey, Map.of("orderId", orderId, "amount", amount));
        } catch (FeignException.NotFound nf) {
            log.info("[TOSS CONFIRM] CARD 결제 – Confirm 호출 불필요({})", nf.status());
        }

        Order order = orderRepo.findByOrderId(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문 없음: " + orderId));
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
        payRepo.findByPaymentKey(paymentKey).ifPresent(p -> {
            p.setPaymentStatus(PaymentStatus.FAIL);
            payRepo.save(p);
            log.warn("[PAYMENT-FAIL] {} -> {}", paymentKey, reason);
        });
    }

    @Override
    @Transactional
    public Map<String, Object> cancelPaymentPoint(String paymentKey, String reason) {
        Payment payment = payRepo.findByPaymentKey(paymentKey)
                .orElseThrow(() -> new IllegalArgumentException("결제 없음: " + paymentKey));

        Map<String, Object> resp = tossClient.cancelPayment(paymentKey, Map.of("cancelReason", reason));
        payment.setPaymentStatus(PaymentStatus.CANCEL);
        payRepo.save(payment);
        return resp;
    }
}