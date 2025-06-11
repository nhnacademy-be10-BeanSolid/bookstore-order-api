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

    private Long userId;

    @Column(name="guest_name")
    private String guestName;

    @Column(name="guest_phone")
    private String guestPhone;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    private LocalDateTime requestedAt;
    private LocalDateTime deliveryAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

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