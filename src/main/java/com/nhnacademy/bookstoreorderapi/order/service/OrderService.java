package com.nhnacademy.bookstoreorderapi.order.service;


import com.nhnacademy.bookstoreorderapi.order.dto.*;
import com.nhnacademy.bookstoreorderapi.order.domain.entity.OrderStatus;

import java.util.List;

public interface OrderService {
    OrderResponseDto createOrder(OrderRequestDto req);
    List<OrderResponseDto> listAll();
    void cancelOrder(Long orderId, String reason);
    StatusChangeResponseDto changeStatus(Long orderId,
                                         OrderStatus newStatus,
                                         Long changedBy,
                                         String memo);
    int requestReturn(Long orderId);
    List<OrderStatusLogDto> getStatusLog(Long orderId);
}