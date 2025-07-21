package com.nhnacademy.bookstoreorderapi.order.service;

import com.nhnacademy.bookstoreorderapi.order.dto.request.CreateOrderRequest;
import com.nhnacademy.bookstoreorderapi.order.dto.request.OrderStatusRequest;
import com.nhnacademy.bookstoreorderapi.order.dto.request.UpdateOrderRequest;
import com.nhnacademy.bookstoreorderapi.order.dto.response.CreateOrderResponse;
import com.nhnacademy.bookstoreorderapi.order.dto.response.OrderDetailResponse;
import com.nhnacademy.bookstoreorderapi.order.dto.response.OrderResponse;
import com.nhnacademy.bookstoreorderapi.order.dto.response.OrderStatusResult;
import com.nhnacademy.bookstoreorderapi.order.dto.response.OrderSummaryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderService {

    CreateOrderResponse createOrder(CreateOrderRequest request, String xUserId);
    CreateOrderResponse getUnfinishedOrder(String orderNumber, String xUserId);
    OrderResponse updateOrder(String orderNumber, UpdateOrderRequest request, String xUserId);
    Page<OrderSummaryResponse> findAllByUserId(String xUserId, Pageable pageable);
    OrderDetailResponse findByOrderNumber(String orderNumber, String xUserId);
    OrderStatusResult changeOrderStatus(String orderNumber, OrderStatusRequest request, String xUserId);
}