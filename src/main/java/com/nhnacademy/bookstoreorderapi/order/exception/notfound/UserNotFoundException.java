package com.nhnacademy.bookstoreorderapi.order.exception.notfound;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String message) {
        super(message);
    }
}
