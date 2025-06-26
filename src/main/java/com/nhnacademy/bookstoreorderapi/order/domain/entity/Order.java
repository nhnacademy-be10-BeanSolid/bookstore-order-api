package com.nhnacademy.bookstoreorderapi.order.domain.entity;

import com.nhnacademy.bookstoreorderapi.order.domain.OrderIdGenerator;
import com.nhnacademy.bookstoreorderapi.order.dto.request.OrderRequest;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

//TODO 주문: 주문인!=받을사람 인 경우가 있을 수 있으니 '수령인' 고려해서 리팩토링 하기.
@Entity
@Table(name = "orders")
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 내부 PK

    private String orderId; // 식별 가능한 주문 번호

    private Long userId; //TODO 회원: 회원 도메인 API로 xUserId -> userId 변환 예정.

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @Column(name = "order_date"/*, columnDefinition = "DATE"*/) // 테스트 환경(h2 database)에서 "DATE"를 인식하지 못해서 임시 조치
    private LocalDate orderDate; // 주문한 날

    private LocalDateTime createdAt; // 주문 데이터가 처음 생성된 시각

    private LocalDateTime updatedAt; // 주문 데이터가 마지막으로 변경된 시각

    private int totalPrice; // 총 상품 금액

    @Embedded
    private ShippingInfo shippingInfo; // 배송 관련 정보

    @Builder.Default
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    public void addItem(OrderItem item) {
        item.setOrder(this);
        this.items.add(item);
    }

    public static Order of(OrderRequest req, Long userId) {

        ShippingInfo shippingInfo = ShippingInfo.of(req, 0);

        return Order.builder()
                .userId(userId)
                .orderDate(LocalDate.now())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now()) //TODO 주문: Auditing 기능 사용해서 구현하기
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