package com.nhnacademy.bookstoreorderapi.order.domain.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "wrappings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Wrapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private Integer price;

    private Boolean isActive;
}
