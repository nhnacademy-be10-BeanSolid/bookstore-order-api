package com.nhnacademy.bookstoreorderapi.order.service;

public interface WrappingService {

    int calculateFinalPrice(int totalPrice, Long wrappingId);
}
