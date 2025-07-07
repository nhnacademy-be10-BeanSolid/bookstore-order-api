package com.nhnacademy.bookstoreorderapi.payment.service.impl;

import com.nhnacademy.bookstoreorderapi.order.domain.entity.Order;
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
                        Optional.ofNullable(resp.get("checkout"))
                                .filter(Map.class::isInstance)
                                .map(Map.class::cast)
                                .map(m -> m.get("url"))
                                .map(Object::toString),
                        Optional.ofNullable(resp.get("checkoutUrl")).map(Object::toString),
                        Optional.ofNullable(resp.get("checkoutPageUrl")).map(Object::toString),
                        Optional.ofNullable(resp.get("paymentUrl")).map(Object::toString),
                        Optional.ofNullable(resp.get("nextRedirectPcUrl")).map(Object::toString)
                )
                .flatMap(Optional::stream)
                .findFirst()
                .orElseThrow(() -> new RedirectUrlNotFoundException(resp));
    }

    @Override
    @Transactional
    public PaymentResDto requestTossPayment(String orderId, PaymentReqDto dto) {
        Order order = orderRepo.findByOrderId(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
        payRepo.findByOrder(order)
                .filter(p -> p.getPaymentStatus() == PaymentStatus.SUCCESS)
                .ifPresent(p -> { throw new AlreadyPaidException(orderId); });

        Map<String, Object> body = Map.of(
                "method",    dto.getPayType() == PayType.ACCOUNT ? "VIRTUAL_ACCOUNT" : dto.getPayType().name(),
                "orderId",   orderId,
                "orderName", dto.getPayName(),
                "amount",    dto.getPayAmount(),
                "successUrl", tossProps.getSuccessUrl(),
                "failUrl",    tossProps.getFailUrl()
        );

        Map<String, Object> resp;
        try {
            resp = tossClient.createPayment(body);
        } catch (FeignException fe) {
            log.error("[TOSS CREATE][ERROR] {}", fe.getMessage(), fe);
            throw new PaymentCreationException("Toss 결제 생성 실패: " + fe.getMessage());
        }

        String key = Optional.ofNullable(resp.get("paymentKey"))
                .map(Object::toString)
                .orElseThrow(() -> new PaymentCreationException("Toss 응답에 paymentKey가 없습니다"));

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

    @Override
    @Transactional
    public void markSuccess(String paymentKey, String orderId, long amount) {
        try {
            tossClient.confirmPayment(paymentKey, Map.of(
                    "orderId", orderId,
                    "amount", String.valueOf(amount)
            ));
        } catch (FeignException.NotFound nf) {
            log.info("[TOSS CONFIRM] Confirm 호출 스킵({})", nf.status());
        } catch (FeignException fe) {
            log.error("[TOSS CONFIRM][ERROR] {}", fe.getMessage(), fe);
            throw new PaymentConfirmationException("Toss 결제 확인 실패: " + fe.getMessage());
        }

        Order order = orderRepo.findByOrderId(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        Payment payment = payRepo.findByOrder(order)
                .orElseGet(() -> Payment.builder()
                        .order(order)
                        .payType(PayType.CARD)
                        .payAmount(amount)
                        .payName("도서 구매")
                        .build()
                );

        payment.setPaymentKey(paymentKey);
        payment.setPaymentStatus(PaymentStatus.SUCCESS);
        payment.setPayAmount(amount);
        payRepo.save(payment);
    }

    @Override
    @Transactional
    public void markFail(String paymentKey, String reason) {
        payRepo.findByPaymentKey(paymentKey)
                .ifPresentOrElse(p -> {
                    p.setPaymentStatus(PaymentStatus.FAIL);
                    payRepo.save(p);
                    log.warn("[PAYMENT-FAIL] {} 이유: {}", paymentKey, reason);
                }, () -> {
                    throw new PaymentNotFoundException(paymentKey);
                });
    }

    // Map 기반 호출 (기존 호환용)
    @Override
    @Transactional
    public Map<String, Object> refundCardPayment(String paymentKey, Map<String, Object> req) {
        // Map → DTO 변환
        CancelPaymentRequest dto = new CancelPaymentRequest(
                Objects.toString(req.get("orderId"), ""),
                ((Number)req.getOrDefault("amount", 0)).longValue(),
                Objects.toString(req.get("cancelReason"), "")
        );
        // 새 메서드 호출
        PaymentResDto result = refundCardPayment(paymentKey, dto);
        // 결과 DTO를 Map으로 변환해 돌려주기
        return Map.of(
                "paymentKey", result.getPaymentKey(),
                "orderId",    result.getOrderId(),
                "payType",    result.getPayType(),
                "payName",    result.getPayName(),
                "amount",     result.getPayAmount(),
                "status",     "CANCELLED"
        );
    }

    // DTO 기반 호출 (신규)
    @Override
    @Transactional
    public PaymentResDto refundCardPayment(String paymentKey, CancelPaymentRequest req) {
        // 1) Toss 환불 API 호출
        try {
            tossClient.cancelPayment(paymentKey, Map.of(
                    "cancelReason", req.getCancelReason(),
                    "cancelAmount", req.getAmount()
            ));
        } catch (FeignException fe) {
            log.error("[TOSS CANCEL][ERROR] {}", fe.getMessage(), fe);
            throw new PaymentCancellationException("Toss 환불 요청 실패: " + fe.getMessage());
        }

        // 2) DB 업데이트
        Payment payment = payRepo.findByPaymentKey(paymentKey)
                .orElseThrow(() -> new PaymentNotFoundException(paymentKey));
        payment.setPaymentStatus(PaymentStatus.CANCEL);
        payRepo.save(payment);

        // 3) DTO 반환
        return PaymentResDto.builder()
                .paymentKey(paymentKey)
                .orderId(req.getOrderId())
                .payType(payment.getPayType().name())
                .payName(payment.getPayName())
                .payAmount(req.getAmount())
                .redirectUrl(null)
                .successUrl(null)
                .failUrl(null)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResDto getPaymentInfo(String paymentKey) {
        Map<String, Object> resp;
        try {
            resp = tossClient.getPaymentInfo(paymentKey);
        } catch (FeignException fe) {
            log.error("[TOSS INFO][ERROR] {}", fe.getMessage(), fe);
            throw new PaymentInfoFetchException("결제 정보 조회 실패: " + fe.getMessage());
        }

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
}