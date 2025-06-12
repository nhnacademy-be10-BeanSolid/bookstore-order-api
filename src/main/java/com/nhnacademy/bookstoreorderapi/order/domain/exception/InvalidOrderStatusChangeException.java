package com.nhnacademy.bookstoreorderapi.order.domain.exception;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidOrderStatusChangeException extends RuntimeException {
    public InvalidOrderStatusChangeException(String message) {
        super(message);
        //주문 상태 전이 규칙에 어긋나는 변경 요청이 들어올때
    }
}