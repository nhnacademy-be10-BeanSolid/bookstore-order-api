package com.nhnacademy.bookstoreorderapi.order.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "canceled_order")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CanceledOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "canceled_order_id")
    private Long canceledOrderId;
    @Column(name ="order_id")
    private Long orderId;

  //취소주문 시간
  @Column(name = "canceled_at", nullable = false)
  private LocalDateTime canceledAt;

  //취소 주문 사유
  @Column(name = "reason", nullable = false)
  private String reason;
}