package com.nhnacademy.bookstoreorderapi.order.service;

import com.nhnacademy.bookstoreorderapi.order.dto.response.OrderResponse;
import com.nhnacademy.bookstoreorderapi.order.dto.response.OrderSummaryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderAdminService {

    Page<OrderSummaryResponse> getAllOrders(Pageable pageable);
    OrderResponse changeStatusToShipping(String orderNumber, String xUserId);
}
