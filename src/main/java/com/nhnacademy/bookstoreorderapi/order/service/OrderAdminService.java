package com.nhnacademy.bookstoreorderapi.order.service;

import com.nhnacademy.bookstoreorderapi.order.dto.request.StatusChangeRequest;
import com.nhnacademy.bookstoreorderapi.order.dto.response.OrderResponse;

public interface OrderAdminService {

    OrderResponse changeStatus(String orderId, StatusChangeRequest request, String xUserId);
}
