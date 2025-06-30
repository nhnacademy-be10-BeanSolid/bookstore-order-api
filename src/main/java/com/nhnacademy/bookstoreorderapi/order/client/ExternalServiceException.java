package com.nhnacademy.bookstoreorderapi.order.client;

public class ExternalServiceException extends RuntimeException {
    public ExternalServiceException(String feignClientError, Throwable t) {
        super(feignClientError, t);
    }
}
