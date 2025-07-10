package com.nhnacademy.bookstoreorderapi.order.service;

import com.nhnacademy.bookstoreorderapi.order.dto.request.OrderRequest;
import com.nhnacademy.bookstoreorderapi.order.dto.request.ReturnRequest;
import com.nhnacademy.bookstoreorderapi.order.dto.request.StatusChangeRequest;
import com.nhnacademy.bookstoreorderapi.order.dto.response.OrderResponse;
import com.nhnacademy.bookstoreorderapi.order.dto.response.OrderSummaryResponse;
import com.nhnacademy.bookstoreorderapi.order.dto.response.PurchaseVerificationResponse;
import org.springframework.data.domain.Page;

public interface OrderService {

    OrderResponse createOrder(OrderRequest orderRequest, String xUserId);
    Page<OrderSummaryResponse> findAllByUserId(String xUserId);
    OrderResponse findByOrderId(String orderId, String xUserId);
    void cancelOrder(String orderId, String reason);
    OrderResponse changeStatus(String orderId, StatusChangeRequest req, String xUserId);
    int requestReturn(String orderId, ReturnRequest req);
//    List<OrderStatusLogDto> getStatusLog(String orderId, String xUserId);
    PurchaseVerificationResponse verifyPurchase(String xUserId, Long bookId);
    void completeDelivery(String orderId);
}