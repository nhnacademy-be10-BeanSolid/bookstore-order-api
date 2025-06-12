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
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userId;  //userId -> 타입 String 변경

    @Column(name="guest_name")
    private String guestName;

    @Column(name="guest_phone")
    private String guestPhone;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    private LocalDateTime requestedAt; // 반품 요청일
    private LocalDateTime deliveryAt; // 배송 요청일
    private LocalDateTime createdAt;   // 처음 저장된 시간
    private LocalDateTime updatedAt;    //주문 정보(상태, 배송일) 마지막으로 수정된 시간

    private int totalPrice;
    private int deliveryFee;
    private int finalPrice;

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