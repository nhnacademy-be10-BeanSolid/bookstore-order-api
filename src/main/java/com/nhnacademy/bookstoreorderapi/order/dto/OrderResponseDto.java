package com.nhnacademy.bookstoreorderapi.order.dto;

import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponseDto implements ResponseDto {
    private Long orderId;
    private int totalPrice;
    private int deliveryFee;
    private int finalPrice;
    private String message;
}