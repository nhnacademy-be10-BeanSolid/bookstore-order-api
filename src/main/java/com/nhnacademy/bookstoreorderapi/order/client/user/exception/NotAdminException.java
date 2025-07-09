package com.nhnacademy.bookstoreorderapi.order.client.user.exception;

public class NotAdminException extends RuntimeException {
    public NotAdminException(String message) {
        super(message);
    }
}
