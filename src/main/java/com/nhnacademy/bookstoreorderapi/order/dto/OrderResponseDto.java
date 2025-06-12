package com.nhnacademy.bookstoreorderapi.order.dto;

import com.nhnacademy.bookstoreorderapi.order.domain.entity.Order;
import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponseDto implements ResponseDto{
    private Long orderId;
    private int totalPrice;
    private int deliveryFee;
    private int finalPrice;
    private String message;
    
    public static OrderResponseDto createFrom(Order order) {

        String userInfo = order.getUserId() != null
                ? "회원 ID: " + order.getUserId()
                : "비회원: " + order.getGuestName() + " (" + order.getGuestPhone() + ")";
        String message = String.format("[%s] 주문 생성됨 / 총액: %d원 / 배송비: %d원 / 결제금액: %d원",
                userInfo, order.getTotalPrice(), order.getDeliveryFee(), order.getFinalPrice());

        return OrderResponseDto.builder()
                .orderId(order.getId())
                .totalPrice(order.getTotalPrice())
                .deliveryFee(order.getDeliveryFee())
                .finalPrice(order.getFinalPrice())
                .message(message)
                .build();
    }
}