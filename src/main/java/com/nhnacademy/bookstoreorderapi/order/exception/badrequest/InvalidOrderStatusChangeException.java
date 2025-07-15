package com.nhnacademy.bookstoreorderapi.order.exception.badrequest;

public class InvalidOrderStatusChangeException extends BadRequestException {
    public InvalidOrderStatusChangeException(String message) {
        super(message);
    }
}