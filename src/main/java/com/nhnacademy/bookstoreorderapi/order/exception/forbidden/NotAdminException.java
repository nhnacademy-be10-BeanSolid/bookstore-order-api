package com.nhnacademy.bookstoreorderapi.order.exception.forbidden;

public class NotAdminException extends RuntimeException {
    public NotAdminException(String message) {
        super(message);
    }
}
