package com.nhnacademy.bookstoreorderapi.order.domain.entity;

import com.nhnacademy.bookstoreorderapi.order.domain.OrderIdGenerator;
import com.nhnacademy.bookstoreorderapi.order.dto.request.OrderRequest;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Order extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 내부 PK

    private String orderId; // 식별 가능한 주문 번호

    private Long userNo;

    @Setter
    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    private LocalDate orderDate; // 주문한 날

    private Long totalPrice; // 총 상품 금액

    @Embedded
    private ShippingInfo shippingInfo; // 배송 관련 정보

    public static Order of(OrderRequest req, Long userNo) {
        long totalAmount = req.orderItems().stream()
                .mapToLong(item -> item.price() * item.quantity())
                .sum();
        int deliveryFee = ShippingInfo.DEFAULT_DELIVERY_FEE;
        if (userNo != null && totalAmount >= 30_000) {
            deliveryFee = 0;
        }

        ShippingInfo shippingInfo = ShippingInfo.of(req, deliveryFee);

        return Order.builder()
                .userNo(userNo)
                .orderDate(LocalDate.now())
                .shippingInfo(shippingInfo)
                .totalPrice(totalAmount)
                .build();
    }

    @PrePersist
    private void ensureOrderId() {
        if (this.orderId == null) {
            this.orderId = OrderIdGenerator.generate();
        }
    }
}