package com.nhnacademy.bookstoreorderapi.order.exception.notfound;

public class OrderNotFoundException extends NotFoundException {
    public OrderNotFoundException(String message) {
        super(message);
    }
}
