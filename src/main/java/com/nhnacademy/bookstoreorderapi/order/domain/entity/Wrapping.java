package com.nhnacademy.bookstoreorderapi.order.domain.entity;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "wrapping")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Wrapping {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "wrapping_id")
    private Long wrappingId;

    @Column(name = "name", nullable = false)
    private String name;

    // 반드시 price 필드명을 사용해야 getPrice() 가 자동 생성됩니다.
    @Column(name = "price", nullable = false)
    private Integer price;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;
}
