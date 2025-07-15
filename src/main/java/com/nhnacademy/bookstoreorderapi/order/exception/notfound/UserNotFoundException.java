package com.nhnacademy.bookstoreorderapi.order.exception.notfound;

public class UserNotFoundException extends NotFoundException {
    public UserNotFoundException(String message) {
        super(message);
    }
}
