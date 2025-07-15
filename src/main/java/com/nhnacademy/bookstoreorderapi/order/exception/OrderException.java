package com.nhnacademy.bookstoreorderapi.order.exception;

import org.springframework.http.HttpStatus;

public abstract class OrderException extends RuntimeException {
    
    public OrderException(String message) {
        super(message);
    }

    public OrderException(String message, Throwable cause) {
        super(message, cause);
    }

    public abstract HttpStatus getHttpStatus();
}