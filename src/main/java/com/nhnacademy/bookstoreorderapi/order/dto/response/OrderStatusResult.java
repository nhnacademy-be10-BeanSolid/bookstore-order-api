package com.nhnacademy.bookstoreorderapi.order.dto.response;

import com.nhnacademy.bookstoreorderapi.payment.dto.Response.PaymentResDto;

public sealed interface OrderStatusResult 
    permits OrderStatusResult.CancelResult, OrderStatusResult.ReturnResult {
    
    record CancelResult(PaymentResDto payment) implements OrderStatusResult {}
    record ReturnResult(OrderResponse order) implements OrderStatusResult {}
}