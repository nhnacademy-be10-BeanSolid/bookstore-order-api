package com.nhnacademy.bookstoreorderapi.payment.exception;

public class PaymentCancellationException extends RuntimeException {
    public PaymentCancellationException(String Payment) {
        super("환불 요청 실패" + Payment);
    }
}
