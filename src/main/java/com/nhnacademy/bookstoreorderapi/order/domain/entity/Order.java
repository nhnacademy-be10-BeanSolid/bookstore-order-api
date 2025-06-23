package com.nhnacademy.bookstoreorderapi.order.domain.entity;

import com.nhnacademy.bookstoreorderapi.order.dto.OrderRequestDto;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "orders")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Order {
    public static final int DEFAULT_DELIVERY_FEE = 5000;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;  // 내부 PK

    @Column(name = "order_id", length = 64, nullable = false, unique = true)
    private String orderId;  // 비즈니스 주문번호

    @Column(name = "user_id")
    private String userId;

    @Column(name = "guest_id")
    private Long guestId;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_status")
    private OrderStatus status;

    @Column(name = "order_date", columnDefinition = "DATE")
    private LocalDate orderDate;

    @Column(name = "requested_delivery_date", columnDefinition = "DATE")
    private LocalDate requestedDeliveryDate;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "total_price")
    private int totalPrice;

    @Column(name = "delivery_fee")
    private int deliveryFee;

    @Column(name = "order_address")
    private String orderAddress;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();

    @PrePersist
    private void ensureOrderId() {
        if (this.orderId == null) {
            this.orderId = UUID.randomUUID().toString();
        }
    }

    public void addItem(OrderItem item) {
        item.setOrder(this);
        this.items.add(item);
    }

    public static Order createFrom(OrderRequestDto req) {
        LocalDate now = LocalDate.now();
        LocalDate reqDate = req.getRequestedDeliveryDate() != null
                ? req.getRequestedDeliveryDate()
                : now;

        Order o = Order.builder()
                .status(OrderStatus.PENDING)
                .orderDate(now)
                .requestedDeliveryDate(reqDate)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .orderAddress(req.getOrderAddress())
                .deliveryFee(DEFAULT_DELIVERY_FEE)
                .totalPrice(0)
                .build();

        if ("member".equalsIgnoreCase(req.getOrderType())) {
            o.setUserId(req.getUserId());
        } else {
            o.setGuestId(req.getGuestId());
        }
        return o;
    }
}