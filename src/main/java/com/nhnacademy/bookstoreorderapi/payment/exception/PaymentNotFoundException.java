package com.nhnacademy.bookstoreorderapi.payment.exception;

/** 결제 PK 조회 실패 시 throw. */
public class PaymentNotFoundException extends RuntimeException {
    public PaymentNotFoundException(Long paymentId) {
        super("Payment not found, id=" + paymentId);
    }
}