package com.nhnacademy.bookstoreorderapi.common.exception;

public class ExternalServiceException extends RuntimeException {
    public ExternalServiceException(String errorMessage, Throwable t) {
        super(errorMessage, t);
    }
}
