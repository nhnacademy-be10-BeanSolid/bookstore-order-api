package com.nhnacademy.bookstoreorderapi.order.exception.notfound;

public class BookNotFoundException extends NotFoundException {
    public BookNotFoundException(String message) {
        super(message);
    }
}
