package com.nhnacademy.bookstoreorderapi.order.dto;

import com.nhnacademy.bookstoreorderapi.order.domain.entity.Order;
import com.nhnacademy.bookstoreorderapi.order.domain.entity.OrderStatus;
import lombok.*;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponseDto {

    private Long orderId;       // 주문 ID
    private int totalPrice;    // 상품 총액
    private int deliveryFee;   // 배송비
    private int finalPrice;    // 결제 금액
    private String message;       // 인포 메시지
    private OrderStatus orderStatus;   // 현재 상태

    public static OrderResponseDto createFrom(Order order) {

        // ① 주문자 라벨
        String userInfo = (order.getUserId() != null)
                ? String.format("회원 ID: %s", order.getUserId())
                : String.format("비회원(guestId=%d)", order.getGuestId());

        // ② 결제 금액 계산(필요 시 엔티티에 getFinalPrice()가 있으면 그대로 사용)
        int finalPrice = order.getTotalPrice() + order.getDeliveryFee();

        // ③ 안내 메시지
        String msg = String.format("[%s] 주문 생성 / 총액 %d원 + 배송비 %d원 → 결제 %d원",
                userInfo,
                order.getTotalPrice(),
                order.getDeliveryFee(),
                finalPrice);   // ← 네 번째 인자 추가

        // ④ DTO 조립
        return OrderResponseDto.builder()
                .orderId(order.getId())
                .totalPrice(order.getTotalPrice())
                .deliveryFee(order.getDeliveryFee())
                .finalPrice(finalPrice)      // ← 필드 채워주기
                .message(msg)
                .orderStatus(order.getStatus())
                .build();
    }
}