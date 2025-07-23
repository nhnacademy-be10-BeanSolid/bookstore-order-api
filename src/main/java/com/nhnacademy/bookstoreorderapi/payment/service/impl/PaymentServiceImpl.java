package com.nhnacademy.bookstoreorderapi.payment.service.impl;

import com.nhnacademy.bookstoreorderapi.common.service.PointService;
import com.nhnacademy.bookstoreorderapi.order.domain.Order;
import com.nhnacademy.bookstoreorderapi.order.domain.OrderStatus;
import com.nhnacademy.bookstoreorderapi.order.exception.notfound.OrderNotFoundException;
import com.nhnacademy.bookstoreorderapi.order.repository.OrderRepository;
import com.nhnacademy.bookstoreorderapi.payment.client.TossPaymentClient;
import com.nhnacademy.bookstoreorderapi.payment.config.TossPaymentConfig;
import com.nhnacademy.bookstoreorderapi.payment.domain.PayType;
import com.nhnacademy.bookstoreorderapi.payment.domain.PaymentStatus;
import com.nhnacademy.bookstoreorderapi.payment.domain.entity.Payment;
import com.nhnacademy.bookstoreorderapi.payment.dto.Request.CancelPaymentRequest;
import com.nhnacademy.bookstoreorderapi.payment.dto.Request.PaymentApprovalRequestDto;
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

import static com.nhnacademy.bookstoreorderapi.order.service.impl.OrderServiceImpl.ORDER_NOTFOUND_MESSAGE;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final OrderRepository orderRepo;
    private final PaymentRepository payRepo;
    private final TossPaymentConfig tossProps;
    private final TossPaymentClient tossClient;
    private final PointService pointService;

    private String extractRedirectUrl(Map<String, Object> resp) {
        return Stream.of(
                        resp.get("nextRedirectPcUrl"),
                        resp.get("nextRedirectMobileUrl"),
                        resp.get("hostedCheckoutUrl"),
                        resp.get("checkoutUrl"),
                        resp.get("checkoutPageUrl"),
                        resp.get("paymentUrl"),
                        Optional.ofNullable(resp.get("checkout"))
                                .filter(Map.class::isInstance)
                                .map(m -> ((Map<?,?>) m).get("url"))
                                .orElse(null)
                )
                .filter(Objects::nonNull)
                .map(Object::toString)
                .findFirst()
                .orElseThrow(() -> new RedirectUrlNotFoundException(resp));
    }

    @Override
    @Transactional
    public PaymentResDto requestTossPayment(String orderNumber, PaymentReqDto dto) {
        Order order = orderRepo.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new OrderNotFoundException(ORDER_NOTFOUND_MESSAGE + orderNumber));

        payRepo.findByOrder(order)
                .filter(p -> p.getPaymentStatus() == PaymentStatus.SUCCESS)
                .ifPresent(p -> { throw new AlreadyPaidException("이미 결제 완료된 주문입니다: orderNumber=" + orderNumber); });

        pointService.validatePointUsage(order.getUserNo(), dto.getUsedPoint());

        Map<String, Object> body = Map.of(
                "method", dto.getPayType() == PayType.ACCOUNT ? "VIRTUAL_ACCOUNT" : dto.getPayType().name(),
                "orderId", orderNumber,
                "orderName", dto.getPayName(),
                "amount", dto.getPayAmount() + order.getShippingInfo().getShippingFee(),
                "successUrl", tossProps.getSuccessUrl(),
                "failUrl", tossProps.getFailUrl()
        );
        Map<String, Object> resp = tossClient.createPayment(body);

        String key = Objects.toString(resp.get("paymentKey"), "");
        if (key.isBlank()) throw new PaymentCreationException("paymentKey 없음");

        Payment payment = payRepo.findByOrder(order).orElseGet(Payment::new);
        payment.setOrder(order);
        payment.setPaymentKey(key);
        payment.setPayType(dto.getPayType());
        payment.setPayAmount(dto.getPayAmount());
        payment.setPayName(dto.getPayName());
        payment.setUsedPoint(dto.getUsedPoint());
        payment.setPaymentStatus(PaymentStatus.PENDING);
        payRepo.save(payment);

        return PaymentResDto.builder()
                .paymentKey(key)
                .orderId(orderNumber)
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
    public PaymentApprovalRequestDto markSuccess(PaymentApprovalRequestDto dto) {
        Payment payment = payRepo.findByPaymentKey(dto.getPaymentKey())
                .orElseThrow(() -> new PaymentNotFoundException(dto.getPaymentKey()));

        PaymentApprovalRequestDto confirmResp;
        try {
            confirmResp = tossClient.confirmPayment(dto);
        } catch (FeignException fe) {
            throw new PaymentConfirmationException("Toss confirm 실패: " + fe.contentUTF8());
        }

        payment.setPaymentStatus(PaymentStatus.SUCCESS);
        payment.setPayAmount(dto.getAmount());
        payRepo.save(payment);

        Order order = orderRepo.findByOrderNumber(dto.getOrderId())
                .orElseThrow(() -> new OrderNotFoundException(ORDER_NOTFOUND_MESSAGE + dto.getOrderId()));
        order.setStatus(OrderStatus.PENDING_PAY);
        orderRepo.save(order);

        pointService.processEarnedPoints(order, payment);
        pointService.processUsedPoints(order, payment);

        return confirmResp;
    }

    @Override
    @Transactional
    public void markFail(String paymentKey, String reason) {
        payRepo.findByPaymentKey(paymentKey).ifPresentOrElse(p -> {
            p.setPaymentStatus(PaymentStatus.FAIL);
            payRepo.save(p);
        }, () -> { throw new PaymentNotFoundException(paymentKey); });
    }

    // 사용하지 않을 예정
    @Override
    @Transactional
    public PaymentResDto refundCardPayment(String paymentKey, CancelPaymentRequest req) {
        Payment payment = payRepo.findByPaymentKey(paymentKey)
                .orElseThrow(() -> new PaymentNotFoundException(paymentKey));

        tossClient.cancelPayment(paymentKey, Map.of(
                "cancelReason", req.getCancelReason(),
                "cancelAmount", req.getAmount()
        ));

        payment.setPaymentStatus(PaymentStatus.CANCEL);
        payment.setPayAmount(req.getAmount());
        payRepo.save(payment);

        return PaymentResDto.builder()
                .paymentKey(paymentKey)
                .orderId(req.getOrderId())
                .payType(payment.getPayType().name())
                .payName(payment.getPayName())
                .payAmount(req.getAmount())
                .build();
    }

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

    @Override
    @Transactional
    public PaymentResDto refundCardPaymentByOrderNumber(String orderNumber, String cancelReason) {
        Order order = orderRepo.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new OrderNotFoundException(ORDER_NOTFOUND_MESSAGE + orderNumber));

        Payment payment = payRepo.findByOrder(order)
                .orElseThrow(() -> new PaymentNotFoundException("결제 정보를 찾을 수 없습니다: orderNumber=" + orderNumber));

        // Toss 결제 취소 API 호출
        tossClient.cancelPayment(payment.getPaymentKey(), Map.of(
                "cancelReason", cancelReason,
                "cancelAmount", payment.getPayAmount()
        ));

        // 결제 상태 업데이트
        payment.setPaymentStatus(PaymentStatus.CANCEL);
        payRepo.save(payment);

        return PaymentResDto.builder()
                .paymentKey(payment.getPaymentKey())
                .orderId(orderNumber)
                .payType(payment.getPayType().name())
                .payName(payment.getPayName())
                .payAmount(payment.getPayAmount())
                .build();
    }
}