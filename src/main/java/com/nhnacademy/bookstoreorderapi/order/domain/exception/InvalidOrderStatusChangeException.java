package com.nhnacademy.bookstoreorderapi.order.domain.exception;

public class InvalidOrderStatusChangeException extends RuntimeException {
    public InvalidOrderStatusChangeException(String message) {
        super(message);
    }
}
