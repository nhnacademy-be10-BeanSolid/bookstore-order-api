package com.nhnacademy.bookstoreorderapi.order.domain;

import com.nhnacademy.bookstoreorderapi.order.dto.request.UpdateOrderRequest;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ShippingInfo {

    public static final Integer FREE_SHIPPING_THRESHOLD = 30_000;
    public static final Integer DEFAULT_SHIPPING_FEE = 5_000;

    //TODO: 배송 관련 정보는 배송 시작 전까지만 수정할 수 있도록 구현 예정.
    @Column(length = 20)
    private String receiverName;

    @Column(length = 20)
    private String receiverPhoneNumber;

    @Column
    private String address;

    @Column
    private LocalDate requestedDeliveryDate;

    @Column
    private Integer shippingFee;

    public ShippingInfo(UpdateOrderRequest request, Integer shippingFee) {
        this.receiverName = request.receiverName();
        this.receiverPhoneNumber = request.receiverPhoneNumber();
        this.address = request.address();
        this.requestedDeliveryDate = request.requestedDeliveryDate();
        this.shippingFee = shippingFee;
    }
}