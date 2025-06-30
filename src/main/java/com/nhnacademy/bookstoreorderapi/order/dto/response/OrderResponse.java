package com.nhnacademy.bookstoreorderapi.order.dto.response;

import com.nhnacademy.bookstoreorderapi.order.domain.entity.Order;
import com.nhnacademy.bookstoreorderapi.order.domain.entity.OrderStatus;

import java.time.LocalDate;

public record OrderResponse(
        Long id, // 주문 내부 PK
        String orderId,      // 주문번호
        OrderStatus status,      // 주문상태 //TODO 주문: enum 직렬화 전략? 생각하기
        LocalDate orderDate,    // 주문일자
        long totalPrice,   // 상품 총액
        int deliveryFee  // 배송비
//        long finalPrice   // 결제 금액 //TODO 결제: 결제금액 및 결제정보를 가져와야할 것 같음.
) {
    public static OrderResponse from(Order o) {

        return new OrderResponse(
                o.getId(),
                o.getOrderId(),
                o.getStatus(),
                o.getOrderDate(),
                o.getTotalPrice(),
                o.getShippingInfo().deliveryFee()
        );
    }
}