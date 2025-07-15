package com.nhnacademy.bookstoreorderapi.order.exception.badrequest;

public class InvalidOrderStatusChangeException extends RuntimeException {
    public InvalidOrderStatusChangeException(String message) {
        super(message);
    }
}