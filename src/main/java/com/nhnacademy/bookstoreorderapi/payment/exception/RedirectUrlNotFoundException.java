package com.nhnacademy.bookstoreorderapi.payment.exception;

public class RedirectUrlNotFoundException extends RuntimeException {
    public RedirectUrlNotFoundException(Object resp) {
        super("리다이렉트 URL 없음" + resp);
    }
}
