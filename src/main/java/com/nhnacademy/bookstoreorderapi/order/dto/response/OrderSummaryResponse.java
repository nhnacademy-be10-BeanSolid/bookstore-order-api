package com.nhnacademy.bookstoreorderapi.order.dto.response;

import com.nhnacademy.bookstoreorderapi.order.domain.entity.Order;

import java.time.LocalDate;

// 회원 주문 목록 조회용
public record OrderSummaryResponse(
    LocalDate orderDate,
    String orderId,
    String receiverName,
    String itemsInfo,
    Long totalPrice
) {
    public static OrderSummaryResponse of(Order o, String bookTitle) {

        int size = o.getItems().size() - 1;
        String itemsInfo = bookTitle + String.format(" 외 %d권", size);

        return new OrderSummaryResponse(o.getOrderDate(),
                o.getOrderId(),
                o.getShippingInfo().receiverName(),
                itemsInfo,
                o.getTotalPrice());
    }
}
