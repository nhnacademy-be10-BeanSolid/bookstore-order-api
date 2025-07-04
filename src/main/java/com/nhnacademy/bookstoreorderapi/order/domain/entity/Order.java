package com.nhnacademy.bookstoreorderapi.order.domain.entity;

import com.nhnacademy.bookstoreorderapi.order.domain.OrderIdGenerator;
import com.nhnacademy.bookstoreorderapi.order.dto.request.OrderRequest;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

//TODO 주문: 주문인!=받을사람 인 경우가 있을 수 있으니 '수령인' 고려해서 리팩토링 하기.
@Entity
@Table(name = "orders")
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Order extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 내부 PK

    private String orderId; // 식별 가능한 주문 번호

    private Long userNo;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    private LocalDate orderDate; // 주문한 날

    private Long totalPrice; // 총 상품 금액

    @Embedded
    private ShippingInfo shippingInfo; // 배송 관련 정보

    @Builder.Default
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    public void addItem(OrderItem item) {
        item.setOrder(this);
        this.items.add(item);
    }

    public static Order of(OrderRequest req, Long userNo) {

        ShippingInfo shippingInfo = ShippingInfo.of(req, 0);

        return Order.builder()
                .userNo(userNo)
                .orderDate(LocalDate.now())
                .shippingInfo(shippingInfo)
                .build();
    }

    @PrePersist
    private void ensureOrderId() {

        if (this.orderId == null) {
            this.orderId = OrderIdGenerator.generate();
        }
    }
}