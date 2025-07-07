package com.nhnacademy.bookstoreorderapi.payment.exception;

public class PaymentCreationException extends RuntimeException {
    public PaymentCreationException(String message) {
        super(message);
    }
}
