package com.nhnacademy.bookstoreorderapi.order.client;

public class ExternalServiceException extends RuntimeException {
    public ExternalServiceException(String errorMessage, Throwable t) {
        super(errorMessage, t);
    }
}
