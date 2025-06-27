package com.nhnacademy.bookstoreorderapi.order.domain.entity;

import com.nhnacademy.bookstoreorderapi.order.client.book.dto.BookOrderResponse;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "order_items")
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_item_id")
    private Long id;

    @Column(name = "book_id")
    private Long bookId;

    @Column(name = "unit_price")
    private int unitPrice;

    @Column(name = "quantity")
    private int quantity;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne
    @JoinColumn(name = "wrapping_id")
    private Wrapping wrapping;

    public static OrderItem of(BookOrderResponse book, int quantity) {

        return OrderItem.builder()
                .bookId(book.id())
                .unitPrice(book.salePrice())
                .quantity(quantity)
                .build();
    }
}
