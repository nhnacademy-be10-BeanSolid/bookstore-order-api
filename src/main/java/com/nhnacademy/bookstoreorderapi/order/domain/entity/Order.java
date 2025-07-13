package com.nhnacademy.bookstoreorderapi.order.domain.entity;

import com.nhnacademy.bookstoreorderapi.order.domain.OrderIdGenerator;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, updatable = false, length = 20)
    private String orderNumber;

    @Column(updatable = false)
    private Long userNo;

    @Column
    @Setter
    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @Column
    private LocalDate orderDate;

    @Column
    private Long totalPrice;

    @Embedded
    private ShippingInfo shippingInfo;

    public Order(Long userNo) {
        this.userNo = userNo;
    }

//    public static Order of(OrderRequest req, Long userNo) {
//        long totalAmount = req.orderItems().stream()
//                .mapToLong(item -> item.price() * item.quantity())
//                .sum();
//        int deliveryFee = ShippingInfo.DEFAULT_DELIVERY_FEE;
//        if (userNo != null && totalAmount >= 30_000) {
//            deliveryFee = 0;
//        }
//
//        ShippingInfo shippingInfo = ShippingInfo.of(req, deliveryFee);
//
//        return Order.builder()
//                .userNo(userNo)
//                .orderDate(LocalDate.now())
//                .shippingInfo(shippingInfo)
//                .totalPrice(totalAmount)
//                .build();
//    }

    @PrePersist
    private void createOrderNumber() {
        if (this.orderNumber == null) {
            this.orderNumber = OrderIdGenerator.generate();
        }
    }
}