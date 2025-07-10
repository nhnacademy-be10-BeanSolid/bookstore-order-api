package com.nhnacademy.bookstoreorderapi.order.common.exception;

public class MissingRequiredParameterException extends RuntimeException {
    public MissingRequiredParameterException(String message) {
        super(message);
    }
}
