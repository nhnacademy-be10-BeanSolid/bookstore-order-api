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

        LocalDate requestedDeliveryDate = req.requestedDeliveryDate() != null
                ? req.requestedDeliveryDate()
                : LocalDate.now().plusDays(1);

        return new ShippingInfo(requestedDeliveryDate,
                req.address(),
                req.receiverName(),
                req.receiverPhoneNumber(),
                deliveryFee);
    }
}
