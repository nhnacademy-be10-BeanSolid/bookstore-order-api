package com.nhnacademy.bookstoreorderapi.order.domain.entity;

import com.nhnacademy.bookstoreorderapi.order.dto.request.OrderRequest;
import jakarta.persistence.Embeddable;

import java.time.LocalDate;

@Embeddable
public record ShippingInfo(
    LocalDate requestedDeliveryDate,
    String address,
    String receiverName,
    String receiverPhoneNumber,
    int deliveryFee
) {
    public static ShippingInfo of(OrderRequest req, int deliveryFee) {

        LocalDate requestedDeliveryDate = req.getRequestedDeliveryDate() != null
                ? req.getRequestedDeliveryDate()
                : LocalDate.now().plusDays(1);

        return new ShippingInfo(requestedDeliveryDate,
                req.getAddress(),
                req.getReceiverName(),
                req.getReceiverPhoneNumber(),
                deliveryFee);
    }
}
