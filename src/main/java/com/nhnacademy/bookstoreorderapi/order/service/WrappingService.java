package com.nhnacademy.bookstoreorderapi.order.service;

import com.nhnacademy.bookstoreorderapi.order.dto.OrderResponseDto;

import java.util.List;

public interface WrappingService {

    List<OrderResponseDto>listByUser(String userId);
    int calculateFinalPrice(int totalPrice, Long wrappingId);
}
