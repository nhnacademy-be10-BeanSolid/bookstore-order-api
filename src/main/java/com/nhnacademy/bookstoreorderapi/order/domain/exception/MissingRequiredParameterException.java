package com.nhnacademy.bookstoreorderapi.order.domain.exception;

public class MissingRequiredParameterException extends RuntimeException {
    public MissingRequiredParameterException(String message) {
        super(message);
    }
}
