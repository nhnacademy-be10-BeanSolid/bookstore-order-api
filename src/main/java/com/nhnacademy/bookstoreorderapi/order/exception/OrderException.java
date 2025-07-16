package com.nhnacademy.bookstoreorderapi.order.exception;

import org.springframework.http.HttpStatus;

public abstract class OrderException extends RuntimeException {
    
    public OrderException(String message) {
        super(message);
    }

    public abstract HttpStatus getHttpStatus();
}