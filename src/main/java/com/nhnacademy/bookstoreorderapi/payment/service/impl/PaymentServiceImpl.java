package com.nhnacademy.bookstoreorderapi.payment.service.impl;

import com.nhnacademy.bookstoreorderapi.common.exception.OrderNotFoundException;
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

    private final OrderRepository orderRepo;
    private final PaymentRepository payRepo;
    private final TossPaymentConfig tossProps;
    private final TossPaymentClient tossClient;

    private String extractRedirectUrl(Map<String, Object> resp) {
        return Stream.of(
                        Optional.ofNullable(resp.get("nextRedirectPcUrl")).map(Object::toString),
                        Optional.ofNullable(resp.get("nextRedirectMobileUrl")).map(Object::toString),
                        Optional.ofNullable(resp.get("hostedCheckoutUrl")).map(Object::toString),
                        Optional.ofNullable(resp.get("checkoutUrl")).map(Object::toString),
                        Optional.ofNullable(resp.get("checkoutPageUrl")).map(Object::toString),
                        Optional.ofNullable(resp.get("paymentUrl")).map(Object::toString),
                        Optional.ofNullable(resp.get("checkout"))
                                .filter(Map.class::isInstance)
                                .map(Map.class::cast)
                                .map(m -> m.get("url"))
                                .map(Object::toString)
                )
                .flatMap(Optional::stream)
                .findFirst()
                .orElseThrow(() -> new RedirectUrlNotFoundException(resp));
    }

    /** Toss 결제 요청 */
    @Override
    @Transactional
    public PaymentResDto requestTossPayment(String orderId, PaymentReqDto dto) {
        Order order = orderRepo.findByOrderId(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        Optional<Payment> oldOpt = payRepo.findByOrder(order);
        if (oldOpt.isPresent() && oldOpt.get().getPaymentStatus() == PaymentStatus.SUCCESS)
            throw new AlreadyPaidException(orderId); // 이미 성공된 주문

        /* Toss 결제 생성 */
        Map<String, Object> body = Map.of(
                "method", dto.getPayType() == PayType.ACCOUNT ? "VIRTUAL_ACCOUNT" : dto.getPayType().name(),
                "orderId", orderId,
                "orderName", dto.getPayName(),
                "amount", dto.getPayAmount(),
                "successUrl", tossProps.getSuccessUrl(),
                "failUrl", tossProps.getFailUrl()
        );
        Map<String, Object> resp = tossClient.createPayment(body);    // 예외는 인터셉터가 처리

        String key = Objects.toString(resp.get("paymentKey"), "");
        if (key.isBlank())
            throw new PaymentCreationException("Toss 응답에 paymentKey가 없습니다");

        Payment payment = oldOpt.orElseGet(Payment::new);            // 기존이 있으면 재사용
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

    /** 결제 성공 콜백 */
    @Override
    @Transactional
    public void markSuccess(String paymentKey, String orderId, long amount) {
        try {
            tossClient.confirmPayment(paymentKey, Map.of("orderId", orderId, "amount", String.valueOf(amount)));
        } catch (FeignException.NotFound nf) {
            log.info("[TOSS CONFIRM] skip {}", nf.status());
        } catch (FeignException fe) {
            throw new PaymentConfirmationException("Toss 결제 확인 실패: " + fe.getMessage());
        }

        Order order = orderRepo.findByOrderId(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        Payment payment = payRepo.findByOrder(order)
                .orElseThrow(() -> new PaymentNotFoundException(paymentKey));

        payment.setPaymentStatus(PaymentStatus.SUCCESS);
        payment.setPayAmount(amount);
        payRepo.save(payment);

        order.setStatus(OrderStatus.PENDING);
    }

    /** 결제 실패 콜백 */
    @Override
    @Transactional
    public void markFail(String paymentKey, String reason) {
        payRepo.findByPaymentKey(paymentKey).ifPresentOrElse(p -> {
            p.setPaymentStatus(PaymentStatus.FAIL);
            payRepo.save(p);
        }, () -> { throw new PaymentNotFoundException(paymentKey); });
    }

    /** 환불(카드·가상계좌 공통) */
    @Override
    @Transactional
    public PaymentResDto refundCardPayment(String paymentKey, CancelPaymentRequest req) {
        Payment payment = payRepo.findByPaymentKey(paymentKey)
                .orElseThrow(() -> new PaymentNotFoundException(paymentKey));

        tossClient.cancelPayment(paymentKey, Map.of(
                "cancelReason", req.getCancelReason(),
                "cancelAmount", req.getAmount()
        )); // payType 분기 제거 → 404 방지

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

    /** 결제 정보 조회 */
    @Override
    @Transactional(readOnly = true)
    public PaymentResDto getPaymentInfo(String paymentKey) {
        Map<String, Object> resp = tossClient.getPaymentInfo(paymentKey);

        return PaymentResDto.builder()
                .paymentKey(paymentKey)
                .orderId(Objects.toString(resp.get("orderId"), ""))
                .payType(Objects.toString(resp.get("method"), ""))
                .payName(Objects.toString(resp.get("orderName"), ""))
                .payAmount(((Number) resp.get("amount")).longValue())
                .redirectUrl(extractRedirectUrl(resp))
                .successUrl(tossProps.getSuccessUrl())
                .failUrl(tossProps.getFailUrl())
                .build();
    }

    /* Map 버전 환불 API는 동일 로직 재사용 */
    @Override
    @Transactional
    public Map<String, Object> refundCardPayment(String paymentKey, Map<String, Object> req) {
        return Map.of("data", refundCardPayment(paymentKey,
                new CancelPaymentRequest(
                        Objects.toString(req.get("orderId"), ""),
                        ((Number) req.getOrDefault("amount", 0)).longValue(),
                        Objects.toString(req.get("cancelReason"), "")
                )));
    }
}