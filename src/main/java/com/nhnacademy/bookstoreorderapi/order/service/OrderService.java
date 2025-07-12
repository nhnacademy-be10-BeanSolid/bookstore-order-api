package com.nhnacademy.bookstoreorderapi.order.service;

import com.nhnacademy.bookstoreorderapi.order.dto.request.CreateOrderRequest;
import com.nhnacademy.bookstoreorderapi.order.dto.response.CreateOrderResponse;

public interface OrderService {

    CreateOrderResponse createOrder(CreateOrderRequest request, String xUserId);
//    Page<OrderSummaryResponse> findAllByUserId(String xUserId, Pageable pageable);
//    OrderDetailResponse findByOrderId(String xUserId, String orderId);
//    void cancelOrder(String orderId, String reason);
//    OrderResponse changeStatus(String orderId, StatusChangeRequest req, String xUserId);
//    int requestReturn(String orderId, ReturnRequest req);
//    List<OrderStatusLogDto> getStatusLog(String orderId, String xUserId);
//    PurchaseVerificationResponse verifyPurchase(String xUserId, Long bookId);
//    void completeDelivery(String orderId);
}