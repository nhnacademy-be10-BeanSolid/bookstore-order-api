package com.nhnacademy.bookstoreorderapi.payment.exception;

public class AlreadyPaidException extends RuntimeException {
    public AlreadyPaidException(String message) {
        super(message);
    }
}
