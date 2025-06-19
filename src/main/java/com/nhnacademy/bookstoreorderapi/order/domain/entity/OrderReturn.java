package com.nhnacademy.bookstoreorderapi.order.domain.entity;

import com.nhnacademy.bookstoreorderapi.order.dto.ReturnRequestDto;
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

    @Column(name = "reason", columnDefinition = "TEXT", nullable = false)
    private String reason;

    @Column(name = "requested_at", nullable = false)
    private LocalDateTime requestedAt;

    @Column(name = "is_damaged", nullable = false)
    private Boolean damaged;

    @Column(name = "refunded_as_points", nullable = false)
    private Boolean refundedAsPoints;

    public static OrderReturn createFrom(Order order, ReturnRequestDto dto) {

        return OrderReturn.builder()
                .order(order)
                .reason(dto.getReason())
                .damaged(dto.getDamaged())
                .requestedAt(dto.getRequestedAt())
                .build();
    }
}
