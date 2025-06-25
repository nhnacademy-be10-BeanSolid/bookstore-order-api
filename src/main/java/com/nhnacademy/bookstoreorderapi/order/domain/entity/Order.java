// src/main/java/com/nhnacademy/bookstoreorderapi/order/domain/entity/Order.java
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

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", length = 64, nullable = false, unique = true)
    private String orderId;

    private String userId;
    private Long guestId;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @Column(columnDefinition = "DATE")
    private LocalDate orderDate;

    @Column(columnDefinition = "DATE")
    private LocalDate requestedDeliveryDate;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private int totalPrice;
    private int deliveryFee;
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

    /** 주문 기본 정보만 초기화 **/
    public static Order createFrom(OrderRequestDto req) {
        LocalDate now = LocalDate.now();
        LocalDate reqDate = (req.getRequestedDeliveryDate() != null)
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