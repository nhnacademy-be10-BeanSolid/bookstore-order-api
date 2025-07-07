package com.nhnacademy.bookstoreorderapi.payment.exception;

public class PaymentConfirmationException extends RuntimeException {
    // 결제 확인(confirm) 호출 중 오류 발생시 던짐
    public PaymentConfirmationException(String message) {
        super(message);
    }
}
