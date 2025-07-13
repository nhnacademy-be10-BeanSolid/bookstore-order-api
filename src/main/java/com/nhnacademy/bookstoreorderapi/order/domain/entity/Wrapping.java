package com.nhnacademy.bookstoreorderapi.order.domain.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "wrappings")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Wrapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Integer price;

    @Column(nullable = false)
    private Boolean active;

    public Wrapping(String name, Integer price, Boolean active) {
        this.name = name;
        this.price = price;
        this.active = active;
    }
}
