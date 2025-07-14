package com.nhnacademy.bookstoreorderapi.order.exception.badrequest;

public class InvalidRequestException extends RuntimeException {
    public InvalidRequestException(String message) {
        super(message);
    }
}
