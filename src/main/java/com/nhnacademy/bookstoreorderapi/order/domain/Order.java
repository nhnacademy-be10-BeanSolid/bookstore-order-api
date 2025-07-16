package com.nhnacademy.bookstoreorderapi.order.domain;

import com.nhnacademy.bookstoreorderapi.order.util.OrderIdGenerator;
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
    @Setter
    private LocalDate orderDate;

    @Column
    @Setter
    private Long totalPrice;

    @Embedded
    @Setter
    private ShippingInfo shippingInfo;

    public Order(Long userNo) {
        this.userNo = userNo;
    }

    @PrePersist
    private void createOrderNumber() {
        if (this.orderNumber == null) {
            this.orderNumber = OrderIdGenerator.generate();
        }
    }
}