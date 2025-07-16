package com.nhnacademy.bookstoreorderapi.order.exception.forbidden;

public class NotAdminException extends ForbiddenException {
    public NotAdminException(String message) {
        super(message);
    }
}
