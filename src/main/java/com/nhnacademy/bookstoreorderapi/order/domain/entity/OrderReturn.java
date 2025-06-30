package com.nhnacademy.bookstoreorderapi.order.domain.entity;

import com.nhnacademy.bookstoreorderapi.order.dto.request.ReturnRequest;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "order_return")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderReturn {

    public static final int RETURNS_FEE = 2_500;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "return_id")
    private Long returnId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

//    @Column(name = "reason", columnDefinition = "TEXT", nullable = false)
    @Lob // 테스트 환경(h2 database)에서 "TEXT"라는 타입이 없기 때문에 임시로 설정해줌.
    private String reason;

    @Column(name = "requested_at", nullable = false)
    private LocalDateTime requestedAt;

    @Column(name = "is_damaged", nullable = false)
    private Boolean damaged;

    @Column(name = "refunded_as_points", nullable = false)
    private Boolean refundedAsPoints;

    public static OrderReturn createFrom(Order order, ReturnRequest dto) {

        return OrderReturn.builder()
                .order(order)
                .reason(dto.reason())
                .damaged(dto.damaged())
                .requestedAt(dto.requestedAt())
                .build();
    }
}
