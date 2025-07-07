package com.nhnacademy.bookstoreorderapi.order.dto.response;

import com.nhnacademy.bookstoreorderapi.order.domain.entity.Order;
import com.nhnacademy.bookstoreorderapi.order.domain.entity.OrderItem;

import java.time.LocalDate;
import java.util.List;

// 회원 주문 목록 조회용
public record OrderSummaryResponse(
    LocalDate orderDate,
    String orderId,
    String receiverName,
    Long totalPrice
) {
    public static OrderSummaryResponse of(Order o, List<OrderItem> items, String bookTitle) {

        int size = items.size() - 1;

        return new OrderSummaryResponse(o.getOrderDate(),
                o.getOrderId(),
                o.getShippingInfo().getReceiverName(),
                o.getTotalPrice());
    }
}
