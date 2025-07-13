package com.nhnacademy.bookstoreorderapi.order.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
    @JoinColumn(updatable = false)
    private Wrapping wrapping;

    @Column(nullable = false, updatable = false)
    private Long bookId;

    @Column(nullable = false, updatable = false)
    private String bookTitle;

    @Column(nullable = false, updatable = false)
    private int unitPrice;

    @Column(nullable = false, updatable = false)
    private int quantity;

    public OrderItem(Long bookId, String bookTitle, int unitPrice, int quantity, Order order) {
        this.bookId = bookId;
        this.bookTitle = bookTitle;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
        this.order = order;
    }

//    public static OrderItem of(BookResponse book, int quantity, Order order, Wrapping wrapping) {
//
//        return OrderItem.builder()
//                .bookId(book.id())
//                .unitPrice(book.salePrice())
//                .quantity(quantity)
//                .order(order)
//                .wrapping(wrapping)
//                .build();
//    }

//    public static List<OrderItem> createItems(Order order,
//                                              List<OrderRequest.OrderItemRequest> requests,
//                                              Map<Long, BookResponse> bookMap,
//                                              Map<Long, Wrapping> wrappingMap) {
//        return requests.stream()
//                .map(req ->
//                        of(bookMap.get(req.bookId()),
//                                req.quantity(),
//                                order,
//                                wrappingMap.get(req.wrappingId()))
//                )
//                .toList();
//    }
}
