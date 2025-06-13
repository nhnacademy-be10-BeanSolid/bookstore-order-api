package com.nhnacademy.bookstoreorderapi.order.domain.entity;

import com.nhnacademy.bookstoreorderapi.order.dto.OrderItemDto;
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
    private Long id;

    @Column(name="order_id", insertable=false, updatable=false)
    private Long orderId;

    private Long bookId;

    private int unitPrice;
    private int quantity;

    @Column(name="gift_wrapped")
    private Boolean giftWrapped;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wrapping_id")
    private Wrapping wrapping;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    public static OrderItem createFrom(OrderItemDto dto, Wrapping wrap, int unitPrice) {

        return OrderItem.builder()
                .bookId(dto.getBookId())
                .quantity(dto.getQuantity())
                .giftWrapped(dto.getGiftWrapped())
                .unitPrice(unitPrice)
                .wrapping(wrap)
                .build();
    }
}