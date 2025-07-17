package com.nhnacademy.bookstoreorderapi.payment.exception;

public class NotEnoughPointException extends RuntimeException {
    public NotEnoughPointException(String message) {
        super(message);
    }
}
