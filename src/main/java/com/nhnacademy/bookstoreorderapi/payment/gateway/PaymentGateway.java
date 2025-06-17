package com.nhnacademy.bookstoreorderapi.payment.gateway;

import com.nhnacademy.bookstoreorderapi.payment.domain.entity.Payment;

/**
 * PG(결제대행) 연동용 스켈레톤.
 * 실제 Toss·Payco 구현은 나중에 이 인터페이스를 구현해서 Bean 으로 등록.
 */
public interface PaymentGateway {
    /** 최초 결제 요청(빌링키 발급·결제창 호출 등) */
    void requestPayment(Payment payment);

    /** 고객 승인(또는 PG 콜백) 이후 최종 매입 확정 */
    void confirmPayment(Payment payment);

    /** 전액·부분 취소 */
    void cancelPayment(Payment payment);
}