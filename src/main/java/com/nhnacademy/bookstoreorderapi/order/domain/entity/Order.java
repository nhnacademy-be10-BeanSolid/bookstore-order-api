package com.nhnacademy.bookstoreorderapi.order.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private String userId;  //userId -> 타입 String 변경

    @Column(name="guest_name")
    private String guestName;

    @Column(name="guest_phone")
    private String guestPhone;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @Column(name = "orderdate_at")
    private LocalDateTime orderdateAt; // 주문일

    @Column(name = "delivery_at")
    private LocalDateTime deliveryAt; // 배송 요청일

    @Column(name = "created_at")
    private LocalDateTime createdAt; // 주문 데이터가 처음 생성된 시각

    @Column(name = "updated_at")
    private LocalDateTime updatedAt; // 주문 데이터가 마지막으로 변경된 시각

    @Column(name = "total_price")
    private int totalPrice; // 총 상품 금액

    @Column(name = "delivery_fee")
    private int deliveryFee; // 배송비

    @Column(name = "final_price")
    private int finalPrice; // 최종 결제 금액


    /**
     * @Builder로 생성할 때 초기화(default)가 무시되는 문제를 막기 위해
     * @Builder.Default를 붙여줍니다.
     */
    @Builder.Default
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    public void addItem(OrderItem item) {
        item.setOrder(this);
        this.items.add(item);
    }
}