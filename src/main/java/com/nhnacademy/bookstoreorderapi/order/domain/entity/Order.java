package com.nhnacademy.bookstoreorderapi.order.domain.entity;

import com.nhnacademy.bookstoreorderapi.order.dto.OrderRequestDto;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
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
    public static final int DEFAULT_DELIVERY_FEE = 5000;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "user_id")
    private String userId;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @Column(name = "order_date", columnDefinition = "DATE")
    private LocalDate orderDate;

    @Column(name = "requested_delivery_date", columnDefinition = "DATE")
    private LocalDate requestedDeliveryDate; // 배송 요청일

    @Column(name = "created_at")
    private LocalDateTime createdAt; // 주문 데이터가 처음 생성된 시각

    @Column(name = "updated_at")
    private LocalDateTime updatedAt; // 주문 데이터가 마지막으로 변경된 시각

    @Column(name = "total_price")
    private int totalPrice; // 총 상품 금액

    @Column(name = "delivery_fee")
    private int deliveryFee; // 배송비

    @Column(name = "guest_id")
    private Long guestId;

    @Column(name = "order_address")
    private String orderAddress; //회원: 장소테이블에서 참조

    @Builder.Default
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    public void addItem(OrderItem item) {
        item.setOrder(this);
        this.items.add(item);
    }

    public static Order createFrom(OrderRequestDto req) {

        LocalDate requestDeliveryDate = req.getDeliveryDate() != null
                ? req.getDeliveryDate()
                : LocalDate.now();

        return Order.builder()
                .userId(req.getUserId())
                .guestName(req.getGuestName())
                .guestPhone(req.getGuestPhone())
                .status(OrderStatus.PENDING)
                .orderDate(LocalDate.now())
                .requestedDeliveryDate(requestDeliveryDate)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .totalPrice(0)
                .deliveryFee(DEFAULT_DELIVERY_FEE)
                .finalPrice(0)
                .build();
    }
}