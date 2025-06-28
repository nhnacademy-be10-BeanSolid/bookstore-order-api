package com.nhnacademy.bookstoreorderapi.order.service;

import com.nhnacademy.bookstoreorderapi.order.dto.*;
import com.nhnacademy.bookstoreorderapi.order.domain.entity.OrderStatus;
import com.nhnacademy.bookstoreorderapi.order.dto.request.OrderRequest;
import com.nhnacademy.bookstoreorderapi.order.dto.response.OrderResponse;
import com.nhnacademy.bookstoreorderapi.order.dto.response.OrderSummaryResponse;

import java.util.List;

public interface OrderService {

    void createOrder(OrderRequest orderRequest, String xUserId);
    List<OrderSummaryResponse> findAllByUserId(String xUserId);
    OrderResponse findByOrderId(String orderId, String xUserId);
    void cancelOrder(Long orderId, String reason);
    StatusChangeResponseDto changeStatus(Long orderId,
                                         OrderStatus newStatus,
                                         Long changedBy,
                                         String memo);
    int requestReturn(Long orderId, ReturnRequestDto req);
    List<OrderStatusLogDto> getStatusLog(Long orderId);
}