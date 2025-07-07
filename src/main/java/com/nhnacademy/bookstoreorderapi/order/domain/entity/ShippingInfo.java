package com.nhnacademy.bookstoreorderapi.order.domain.entity;

import com.nhnacademy.bookstoreorderapi.order.dto.request.OrderRequest;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ShippingInfo {

    public static Integer DEFAULT_DELIVERY_FEE = 5_000;

    private String receiverName;
    private String receiverPhoneNumber;
    private String address;
    private LocalDate requestedDeliveryDate;
    private Integer deliveryFee;


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
