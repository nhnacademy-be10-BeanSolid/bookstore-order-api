package com.nhnacademy.bookstoreorderapi.order.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "order_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, updatable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    @Setter
    private Wrapping wrapping;

    @Column(nullable = false, updatable = false)
    private Long bookId;

    @Column(nullable = false, updatable = false)
    private String bookTitle;

    @Column(nullable = false, updatable = false)
    private Integer unitPrice;

    @Column(nullable = false, updatable = false)
    private Integer quantity;

    public OrderItem(Long bookId, String bookTitle, int unitPrice, int quantity, Order order) {
        this.bookId = bookId;
        this.bookTitle = bookTitle;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
        this.order = order;
    }
}