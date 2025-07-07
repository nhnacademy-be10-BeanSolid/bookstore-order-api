package com.nhnacademy.bookstoreorderapi.order.client.user.exception;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String message) {
        super(message);
    }
}
