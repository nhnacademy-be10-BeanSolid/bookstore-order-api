package com.nhnacademy.bookstoreorderapi.order.exception.notfound;

public class BookNotFoundException extends RuntimeException {
    public BookNotFoundException(String message) {
        super(message);
    }
}
