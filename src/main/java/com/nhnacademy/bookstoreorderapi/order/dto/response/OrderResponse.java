package com.nhnacademy.bookstoreorderapi.order.dto.response;

import com.nhnacademy.bookstoreorderapi.order.domain.entity.Order;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class OrderResponse {

    private Long id; // 주문 내부 PK
    private String orderId; // 주문번호
    private String status;
    private LocalDate orderDate;
    private String receiverName;
    private String receiverPhoneNumber;
    private String address;
    private LocalDate requestedDeliveryDate;
    private Integer deliveryFee;
    private Long totalAmount;

    public static OrderResponse from(Order o) {
        String statusName = o.getStatus() == null ? null : o.getStatus().name();

        return new OrderResponse(
                o.getId(),
                o.getOrderId(),
                statusName,
                o.getOrderDate(),
                o.getShippingInfo().getReceiverName(),
                o.getShippingInfo().getReceiverPhoneNumber(),
                o.getShippingInfo().getAddress(),
                o.getShippingInfo().getRequestedDeliveryDate(),
                o.getShippingInfo().getDeliveryFee(),
                o.getTotalPrice()
        );
    }
}