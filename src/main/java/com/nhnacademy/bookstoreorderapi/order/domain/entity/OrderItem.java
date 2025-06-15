package com.nhnacademy.bookstoreorderapi.order.domain.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "order_item")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_item_id")
    private Long orderItemId;

    @Column(name = "order_id", insertable = false, updatable = false)
    private Long orderId;

    @Column(name = "book_id")
    private Long bookId;

    @Column(name = "unit_price")
    private int unitPrice;

    @Column(name = "quantity")
    private int quantity;

    @Column(name = "is_gift_wrapped")
    private Boolean isGiftWrapped;

    @Column(name = "wrapping_id", insertable = false, updatable = false)
    private Long wrappingId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wrapping_id")
    private Wrapping wrapping;
}