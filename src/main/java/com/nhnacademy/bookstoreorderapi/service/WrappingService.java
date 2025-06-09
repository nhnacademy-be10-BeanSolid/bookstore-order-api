package com.nhnacademy.bookstoreorderapi.service;

public interface WrappingService {

    int calculateFinalPrice(int totalPrice, Long wrappingId);
}
