package com.nhnacademy.bookstoreorderapi.order.service;

import com.nhnacademy.bookstoreorderapi.order.dto.request.StatusChangeRequest;
import com.nhnacademy.bookstoreorderapi.order.dto.response.OrderResponse;
import com.nhnacademy.bookstoreorderapi.order.dto.response.OrderSummaryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface OrderAdminService {

    Page<OrderSummaryResponse> getAllOrders(String xUserId, Pageable pageable);
    OrderResponse changeStatus(String orderId, StatusChangeRequest request, String xUserId);
}
