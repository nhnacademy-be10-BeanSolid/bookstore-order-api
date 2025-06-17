package com.nhnacademy.bookstoreorderapi.payment.service;

import java.util.List;

import com.nhnacademy.bookstoreorderapi.payment.domain.PaymentStatus;
import com.nhnacademy.bookstoreorderapi.payment.domain.entity.Payment;
import com.nhnacademy.bookstoreorderapi.payment.domain.PaymentMethod;

/**
 * 결제 도메인 비즈니스 API.
 */
public interface PaymentService {

    /** 결제 생성(결제창 호출 전 단계) */
    Payment createPayment(Long orderId,
                          PaymentMethod method,
                          String provider,
                          int amount);

    /** 승인 완료 콜백 처리 */
    Payment approvePayment(Long paymentId);

    /** 전액·부분 취소 */
    Payment cancelPayment(Long paymentId, int cancelAmount, String reason);

    /** 단건 조회 */
    Payment getPayment(Long paymentId);

    /** 주문별 조회 */
    List<Payment> getPaymentsByOrder(Long orderId);

    /** 상태별 조회 */
    List<Payment> getPaymentsByStatus(PaymentStatus status);
}