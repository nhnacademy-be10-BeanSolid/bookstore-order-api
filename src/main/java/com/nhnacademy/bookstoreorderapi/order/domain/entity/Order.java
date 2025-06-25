package com.nhnacademy.bookstoreorderapi.order.domain.entity;

import com.nhnacademy.bookstoreorderapi.order.domain.OrderIdGenerator;
import com.nhnacademy.bookstoreorderapi.order.dto.request.OrderRequest;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

//    @Column(length = 6)
    private String orderId;

    @Column(name = "user_id")
    private String userId; // X-User-Id

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @Column(name = "order_date"/*, columnDefinition = "DATE"*/) // 테스트 환경(h2 database)에서 "DATE"를 인식하지 못해서 임시 조치
    private LocalDate orderDate; // 주문한 날

    @Column(name = "requested_delivery_date"/*, columnDefinition = "DATE"*/)
    private LocalDate requestedDeliveryDate; // 배송 요청일

    @Column(name = "created_at")
    private LocalDateTime createdAt; // 주문 데이터가 처음 생성된 시각

    @Column(name = "updated_at")
    private LocalDateTime updatedAt; // 주문 데이터가 마지막으로 변경된 시각

    @Column(name = "total_price")
    private int totalPrice; // 총 상품 금액

    @Column(name = "delivery_fee")
    private int deliveryFee; // 배송비

    @Column(name = "address")
    private String address; // 배송지

    @Builder.Default
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    public void addItem(OrderItem item) {
        item.setOrder(this);
        this.items.add(item);
    }

    public static Order of(OrderRequest req, String userId) {

        LocalDate requestDeliveryDate = req.getRequestedDeliveryDate() != null
                ? req.getRequestedDeliveryDate()
                : LocalDate.now().plusDays(1);

        return Order.builder()
                .userId(userId)
                .orderDate(LocalDate.now())
                .requestedDeliveryDate(requestDeliveryDate)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now()) //TODO 99: Auditing 기능 사용해서 구현하기
                .address(req.getAddress())
                .build();
    }

    @PrePersist
    private void ensureOrderId() {

        if (this.orderId == null) {
            this.orderId = OrderIdGenerator.generate();
        }
    }
}