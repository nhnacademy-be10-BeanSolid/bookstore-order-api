package com.nhnacademy.bookstoreorderapi.order.exception.notfound;

public class OrderNotFoundException extends RuntimeException {
    public OrderNotFoundException(String orderNumber) {
        super("주문을 찾을 수 없습니다: orderNumber=" + orderNumber);
    }
}
