package com.nhnacademy.bookstoreorderapi.order.domain.entity;

import com.nhnacademy.bookstoreorderapi.order.dto.request.OrderRequest;
import jakarta.persistence.Embeddable;

import java.time.LocalDate;

@Embeddable
public record ShippingInfo(
        String receiverName,
        String receiverPhoneNumber,
        String address,
        LocalDate requestedDeliveryDate,
        Integer deliveryFee
) {
    public static Integer DEFAULT_DELIVERY_FEE = 5_000;

    public static ShippingInfo of(OrderRequest req, int deliveryFee) {
        LocalDate requestedDeliveryDate = req.requestedDeliveryDate() != null
                ? req.requestedDeliveryDate()
                : LocalDate.now().plusDays(1);

        String address = String.join(" ", req.zipCode(), req.baseAddress(), req.detailAddress());

        return new ShippingInfo(
                req.receiverName(),
                req.receiverPhoneNumber(),
                address,
                requestedDeliveryDate,
                deliveryFee);
    }
}
