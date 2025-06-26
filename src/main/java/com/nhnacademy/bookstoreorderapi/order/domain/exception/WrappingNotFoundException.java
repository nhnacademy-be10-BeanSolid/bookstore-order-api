package com.nhnacademy.bookstoreorderapi.order.domain.exception;

public class WrappingNotFoundException extends RuntimeException {
    public WrappingNotFoundException(String message) {
        super(message);
    }
}
