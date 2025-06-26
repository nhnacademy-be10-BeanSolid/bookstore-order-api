package com.nhnacademy.bookstoreorderapi.order.service;

import com.nhnacademy.bookstoreorderapi.order.dto.response.OrderResponse;

import java.util.List;

public interface WrappingService {

    List<OrderResponse>listByUser(String userId);
    int calculateFinalPrice(int totalPrice, Long wrappingId);
}
