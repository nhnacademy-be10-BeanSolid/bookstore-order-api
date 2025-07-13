package com.nhnacademy.bookstoreorderapi.order.domain.entity;

import com.nhnacademy.bookstoreorderapi.order.dto.request.OrderRequest;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor //TODO: 코드 리팩토링 후 쓸 일 없다면 제거 예정
public class ShippingInfo {

    public static final Integer DEFAULT_DELIVERY_FEE = 5_000;

    //TODO: 배송 관련 정보는 배송 시작 전까지만 수정할 수 있도록 구현 예정.
    @Column(length = 20)
    private String receiverName;

    @Column(length = 20)
    private String receiverPhoneNumber;

    @Column
    private String address;

    @Column
    private LocalDate requestedDeliveryDate;

    @Column(updatable = false)
    private Integer deliveryFee;

    public static ShippingInfo of(OrderRequest req, int deliveryFee) {
        LocalDate requestedDeliveryDate = req.requestedDeliveryDate() != null
                ? req.requestedDeliveryDate()
                : LocalDate.now().plusDays(1);

        return new ShippingInfo(
                req.receiverName(),
                req.receiverPhoneNumber(),
                req.deliveryAddress(),
                requestedDeliveryDate,
                deliveryFee);
    }
}
