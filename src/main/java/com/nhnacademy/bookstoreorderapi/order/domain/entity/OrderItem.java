package com.nhnacademy.bookstoreorderapi.order.domain.entity;

import com.nhnacademy.bookstoreorderapi.order.client.book.dto.BookResponse;
import com.nhnacademy.bookstoreorderapi.order.dto.request.OrderRequest;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.Map;

@Entity
@Table(name = "order_items")
@Getter
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

    public static OrderItem of(BookResponse book, int quantity, Order order, Wrapping wrapping) {

        return OrderItem.builder()
                .bookId(book.id())
                .unitPrice(book.salePrice())
                .quantity(quantity)
                .order(order)
                .wrapping(wrapping)
                .build();
    }

    public static List<OrderItem> createItems(Order order,
                                              List<OrderRequest.OrderItemRequest> requests,
                                              Map<Long, BookResponse> bookMap,
                                              Map<Long, Wrapping> wrappingMap) {
        return requests.stream()
                .map(req ->
                        of(bookMap.get(req.bookId()),
                                req.quantity(),
                                order,
                                wrappingMap.get(req.wrappingId()))
                )
                .toList();
    }
}
