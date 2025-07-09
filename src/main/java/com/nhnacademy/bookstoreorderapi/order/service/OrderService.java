package com.nhnacademy.bookstoreorderapi.order.service;

import com.nhnacademy.bookstoreorderapi.order.domain.entity.OrderStatus;
import com.nhnacademy.bookstoreorderapi.order.dto.OrderStatusLogDto;
import com.nhnacademy.bookstoreorderapi.order.dto.StatusChangeResponseDto;
import com.nhnacademy.bookstoreorderapi.order.dto.request.OrderRequest;
import com.nhnacademy.bookstoreorderapi.order.dto.request.ReturnRequest;
import com.nhnacademy.bookstoreorderapi.order.dto.response.OrderResponse;
import com.nhnacademy.bookstoreorderapi.order.dto.response.OrderSummaryResponse;
import com.nhnacademy.bookstoreorderapi.order.dto.response.PurchaseVerificationResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface OrderService {

    OrderResponse createOrder(OrderRequest orderRequest, String xUserId);
    Page<OrderSummaryResponse> findAllByUserId(String xUserId);
    OrderResponse findByOrderId(String orderId, String xUserId);
    void cancelOrder(String orderId, String reason);
    StatusChangeResponseDto changeStatus(String orderId,
                                         OrderStatus newStatus,
                                         String memo,
                                         String xUserId);
    int requestReturn(String orderId, ReturnRequest req);
    List<OrderStatusLogDto> getStatusLog(String orderId, String xUserId);
    PurchaseVerificationResponse verifyPurchase(String xUserId, Long bookId);
}