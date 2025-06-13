package com.nhnacademy.bookstoreorderapi.order.domain.exception;

public class OrderNotFoundException extends RuntimeException {
    public OrderNotFoundException(String message) {
        super(message);
    }
    //주문을 조회하려 했으나 해당 ID의 주문이 DB에 없을때
}
