package com.nhnacademy.bookstoreorderapi.order.exception.forbidden;

import com.nhnacademy.bookstoreorderapi.order.exception.OrderException;
import org.springframework.http.HttpStatus;

public class ForbiddenException extends OrderException {
    
    public ForbiddenException(String message) {
        super(message);
    }

    public ForbiddenException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.FORBIDDEN;
    }
}
