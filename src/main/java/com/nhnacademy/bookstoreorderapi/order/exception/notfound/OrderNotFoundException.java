package com.nhnacademy.bookstoreorderapi.order.exception.notfound;

public class OrderNotFoundException extends RuntimeException {
    public OrderNotFoundException(String message) {
        super(message);
    }
}
