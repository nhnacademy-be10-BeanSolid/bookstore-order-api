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
    private Long id;

    private Long bookId;

    private int unitPrice;

    private int quantity;

    @ManyToOne
    private Order order;

    @ManyToOne
    private Wrapping wrapping;

    public static OrderItem of(BookOrderResponse book, int quantity) {

        return OrderItem.builder()
                .bookId(book.id())
                .unitPrice(book.salePrice())
                .quantity(quantity)
                .build();
    }
}
