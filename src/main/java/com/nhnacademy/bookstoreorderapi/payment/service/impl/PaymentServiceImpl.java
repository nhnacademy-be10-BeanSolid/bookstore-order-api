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

    /** Map 응답에서 첫 번째 non-null URL을 꺼냅니다. */
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

    /** Toss 결제 요청 생성 */
    @Override
    @Transactional
    public PaymentResDto requestTossPayment(String orderId, PaymentReqDto dto) {
        // 1) 주문 검증
        Order order = orderRepo.findByOrderId(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        // 2) 이미 결제됨 확인
        payRepo.findByOrder(order)
                .filter(p -> p.getPaymentStatus() == PaymentStatus.SUCCESS)
                .ifPresent(p -> { throw new AlreadyPaidException(orderId); });

        // 3) Toss API 호출
        Map<String, Object> body = Map.of(
                "method",     dto.getPayType() == PayType.ACCOUNT ? "VIRTUAL_ACCOUNT" : dto.getPayType().name(),
                "orderId",    orderId,
                "orderName",  dto.getPayName(),
                "amount",     dto.getPayAmount(),
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

        // 4) paymentKey 검증
        String key = Optional.ofNullable(resp.get("paymentKey"))
                .map(Object::toString)
                .orElseThrow(() -> new PaymentCreationException("Toss 응답에 paymentKey가 없습니다"));

        Payment pending = Payment.builder()
                .order(order)
                .paymentKey(key)
                .payType(dto.getPayType())
                .payAmount(dto.getPayAmount())
                .payName(dto.getPayName())
                .paymentStatus(PaymentStatus.PENDING)
                .build();
        payRepo.save(pending);

        // 5) DTO 구성
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

    /** 결제 성공 콜백 처리 */
    @Override
    @Transactional
    public void markSuccess(String paymentKey, String orderId, long amount) {
        // Toss confirm 호출 (NotFound만 무시)
        try {
            tossClient.confirmPayment(paymentKey, Map.of(
                    "orderId", orderId,
                    "amount",  String.valueOf(amount)
            ));
        } catch (FeignException.NotFound nf) {
            log.info("[TOSS CONFIRM] 스킵({})", nf.status());
        } catch (FeignException fe) {
            log.error("[TOSS CONFIRM][ERROR] {}", fe.getMessage(), fe);
            throw new PaymentConfirmationException("Toss 결제 확인 실패: " + fe.getMessage());
        }

        // 주문 존재 여부 재검증
        Order order = orderRepo.findByOrderId(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        // DB에 성공 처리
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

    /** 결제 실패 콜백 처리 */
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

    // ─────────────────────────────────────────────────────
    // (6) 카드 결제 환불 (레거시 Map 기반)
    @Override
    @Transactional
    public Map<String, Object> refundCardPayment(String paymentKey, Map<String, Object> req) {
        // Map → DTO 변환
        CancelPaymentRequest dto = new CancelPaymentRequest(
                Objects.toString(req.get("orderId"), ""),
                ((Number) req.getOrDefault("amount", 0)).longValue(),
                Objects.toString(req.get("cancelReason"), "")
        );
        // 신규 메서드 호출
        PaymentResDto result = refundCardPayment(paymentKey, dto);
        // 결과를 Map으로 변환해 반환
        return Map.of(
                "paymentKey", result.getPaymentKey(),
                "orderId",    result.getOrderId(),
                "payType",    result.getPayType(),
                "payName",    result.getPayName(),
                "amount",     result.getPayAmount(),
                "status",     "CANCELLED"
        );
    }

    // (7) 결제 취소/환불 처리 (DTO 기반)
    @Override
    @Transactional
    public PaymentResDto refundCardPayment(String paymentKey, CancelPaymentRequest req) {
        // 1) DB에서 기존 결제 조회
        Payment payment = payRepo.findByPaymentKey(paymentKey)
                .orElseThrow(() -> new PaymentNotFoundException(paymentKey));

        Map<String,Object> resp;
        try {
            // 2) 가상계좌(PENDING) vs 카드환불 분기
            if (payment.getPayType() == PayType.ACCOUNT
                    && payment.getPaymentStatus() == PaymentStatus.PENDING) {
                // 가상계좌 미입금 취소
                resp = tossClient.cancelPayment(paymentKey, Map.of(
                        "cancelReason", req.getCancelReason(),
                        "cancelAmount",  req.getAmount()
                ));
            } else {
                // 카드 결제 환불
                resp = tossClient.refundPayment(paymentKey, Map.of(
                        "cancelReason", req.getCancelReason(),
                        "cancelAmount",  req.getAmount()
                ));
            }
        } catch (FeignException fe) {
            log.error("[TOSS REFUND][ERROR] {}", fe.getMessage(), fe);
            throw new PaymentCancellationException("Toss 환불/취소 요청 실패: " + fe.getMessage());
        }

        // 3) DB 상태 업데이트
        payment.setPaymentStatus(PaymentStatus.CANCEL);
        payRepo.save(payment);

        // 4) DTO로 결과 리턴
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