package com.nhnacademy.bookstoreorderapi.payment.service.impl;

import com.nhnacademy.bookstoreorderapi.order.domain.entity.Order;
import com.nhnacademy.bookstoreorderapi.order.domain.entity.OrderStatus;
import com.nhnacademy.bookstoreorderapi.order.repository.OrderRepository;
import com.nhnacademy.bookstoreorderapi.payment.client.TossPaymentClient;
import com.nhnacademy.bookstoreorderapi.payment.config.TossPaymentConfig;
import com.nhnacademy.bookstoreorderapi.payment.domain.PayType;
import com.nhnacademy.bookstoreorderapi.payment.domain.PaymentStatus;
import com.nhnacademy.bookstoreorderapi.payment.domain.entity.Payment;
import com.nhnacademy.bookstoreorderapi.payment.dto.Request.CancelPaymentRequest;
import com.nhnacademy.bookstoreorderapi.payment.dto.Request.PaymentReqDto;
import com.nhnacademy.bookstoreorderapi.payment.dto.Response.PaymentResDto;
import com.nhnacademy.bookstoreorderapi.payment.exception.*;
import com.nhnacademy.bookstoreorderapi.payment.repository.PaymentRepository;
import com.nhnacademy.bookstoreorderapi.payment.service.PaymentService;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final OrderRepository     orderRepo;
    private final PaymentRepository   payRepo;
    private final TossPaymentConfig   tossProps;
    private final TossPaymentClient   tossClient;

    // 리다이렉트 URL 추출
    private String extractRedirectUrl(Map<String, Object> resp) {
        return Stream.of(
                        Optional.ofNullable(resp.get("nextRedirectPcUrl")),
                        Optional.ofNullable(resp.get("nextRedirectMobileUrl")),
                        Optional.ofNullable(resp.get("hostedCheckoutUrl")),
                        Optional.ofNullable(resp.get("checkoutUrl")),
                        Optional.ofNullable(resp.get("checkoutPageUrl")),
                        Optional.ofNullable(resp.get("paymentUrl")),
                        Optional.ofNullable(resp.get("checkout"))
                                .filter(Map.class::isInstance)
                                .map(x -> ((Map<?,?>)x).get("url"))
                )
                .filter(Objects::nonNull)
                .map(Object::toString)
                .findFirst()
                .orElseThrow(() -> new RedirectUrlNotFoundException(resp));
    }

    // Toss 결제 생성
    @Override @Transactional
    public PaymentResDto requestTossPayment(String orderId, PaymentReqDto dto) {

        dto.setOrderId(orderId);                       // ★ 필수

        Order order = orderRepo.findByOrderId(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        payRepo.findByOrder(order)
                .filter(p -> p.getPaymentStatus() == PaymentStatus.SUCCESS)
                .ifPresent(p -> { throw new AlreadyPaidException(orderId); });

        Map<String,Object> body = Map.of(
                "method"     , dto.getPayType() == PayType.ACCOUNT ? "VIRTUAL_ACCOUNT"
                        : dto.getPayType().name(),
                "orderId"    , orderId,
                "orderName"  , dto.getPayName(),
                "amount"     , dto.getPayAmount(),
                "successUrl" , tossProps.getSuccessUrl(),
                "failUrl"    , tossProps.getFailUrl()
        );

        Map<String,Object> resp = tossClient.createPayment(body);

        String key = Objects.toString(resp.get("paymentKey"), "");
        if (key.isBlank()) throw new PaymentCreationException("paymentKey 없음");

        Payment payment = payRepo.findByOrder(order).orElseGet(Payment::new);
        payment.setOrder(order);
        payment.setPaymentKey(key);
        payment.setPayType(dto.getPayType());
        payment.setPayAmount(dto.getPayAmount());
        payment.setPayName(dto.getPayName());
        payment.setPaymentStatus(PaymentStatus.PENDING);
        payRepo.save(payment);

        return PaymentResDto.builder()
                .paymentKey(key)
                .orderId(orderId)
                .payType(dto.getPayType().name())
                .payName(dto.getPayName())
                .payAmount(dto.getPayAmount())
                .redirectUrl(extractRedirectUrl(resp))
                .successUrl(tossProps.getSuccessUrl())
                .failUrl(tossProps.getFailUrl())
                .build();
    }

    // 결제 성공 확정
    @Override @Transactional
    public void markSuccess(String paymentKey, String orderId, long amount) {

        // Toss confirm 호출
        try {
            tossClient.confirmPayment(paymentKey,
                    Map.of("orderId", orderId, "amount", amount));   // ★ 숫자 그대로
        } catch (FeignException.NotFound nf) {
            log.info("[TOSS CONFIRM] not-found {}", nf.status());    // 결제 이미 승인된 경우
        } catch (FeignException fe) {
            throw new PaymentConfirmationException("Toss confirm 실패: " + fe.getMessage());
        }

        Order order = orderRepo.findByOrderId(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        Payment payment = payRepo.findByOrder(order)
                .orElseThrow(() -> new PaymentNotFoundException(paymentKey));

        payment.setPaymentStatus(PaymentStatus.SUCCESS);
        payment.setPayAmount(amount);
        payRepo.save(payment);

        order.setStatus(OrderStatus.PENDING);
        orderRepo.save(order);
    }

    // 결제 실패
    @Override @Transactional
    public void markFail(String paymentKey, String reason) {
        payRepo.findByPaymentKey(paymentKey).ifPresentOrElse(p -> {
            p.setPaymentStatus(PaymentStatus.FAIL);
            payRepo.save(p);
        }, () -> { throw new PaymentNotFoundException(paymentKey); });
    }

    // 환불
    @Override @Transactional
    public PaymentResDto refundCardPayment(String paymentKey, CancelPaymentRequest req) {

        Payment payment = payRepo.findByPaymentKey(paymentKey)
                .orElseThrow(() -> new PaymentNotFoundException(paymentKey));

        tossClient.cancelPayment(paymentKey, Map.of(
                "cancelReason", req.getCancelReason(),
                "cancelAmount", req.getAmount()
        ));

        payment.setPaymentStatus(PaymentStatus.CANCEL);
        payRepo.save(payment);

        return PaymentResDto.builder()
                .paymentKey(paymentKey)
                .orderId(req.getOrderId())
                .payType(payment.getPayType().name())
                .payName(payment.getPayName())
                .payAmount(req.getAmount())
                .build();
    }

    // 결제 조회
    @Override @Transactional(readOnly = true)
    public PaymentResDto getPaymentInfo(String paymentKey) {
        Map<String,Object> resp = tossClient.getPaymentInfo(paymentKey);

        return PaymentResDto.builder()
                .paymentKey(paymentKey)
                .orderId(Objects.toString(resp.get("orderId"), ""))
                .payType(Objects.toString(resp.get("method"), ""))
                .payName(Objects.toString(resp.get("orderName"), ""))
                .payAmount(((Number)resp.get("amount")).longValue())
                .redirectUrl(extractRedirectUrl(resp))
                .successUrl(tossProps.getSuccessUrl())
                .failUrl(tossProps.getFailUrl())
                .build();
    }

    // Map 기반 환불 재사용
    @Override @Transactional
    public Map<String,Object> refundCardPayment(String paymentKey, Map<String,Object> req) {
        return Map.of("data", refundCardPayment(paymentKey,
                new CancelPaymentRequest(
                        Objects.toString(req.get("orderId"), ""),
                        ((Number)req.getOrDefault("amount", 0)).longValue(),
                        Objects.toString(req.get("cancelReason"), "")
                )));
    }
}