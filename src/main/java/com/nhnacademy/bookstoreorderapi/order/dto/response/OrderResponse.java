package com.nhnacademy.bookstoreorderapi.order.dto.response;

import com.nhnacademy.bookstoreorderapi.order.domain.entity.Order;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class OrderResponse {

    private String orderNumber;
    private Long userNo;
    private String status;
    private LocalDate orderDate;
    private Long totalPrice;
    private String receiverName;
    private String receiverPhoneNumber;
    private String address;
    private LocalDate requestedDeliveryDate;
    private Integer shippingFee;

    public static OrderResponse from(Order o) {
        String statusName = o.getStatus() == null ? null : o.getStatus().name();

        return new OrderResponse(
                o.getOrderNumber(),
                o.getUserNo(),
                statusName,
                o.getOrderDate(),
                o.getTotalPrice(),
                o.getShippingInfo().getReceiverName(),
                o.getShippingInfo().getReceiverPhoneNumber(),
                o.getShippingInfo().getAddress(),
                o.getShippingInfo().getRequestedDeliveryDate(),
                o.getShippingInfo().getShippingFee()
        );
    }
}