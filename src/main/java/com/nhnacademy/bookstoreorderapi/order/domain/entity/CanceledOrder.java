package com.nhnacademy.bookstoreorderapi.order.domain.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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
    private Long id;

    private Long orderId;

  //취소주문 시간
    private LocalDateTime canceledAt;

  //취소 주문 사유
    private String reason;
}