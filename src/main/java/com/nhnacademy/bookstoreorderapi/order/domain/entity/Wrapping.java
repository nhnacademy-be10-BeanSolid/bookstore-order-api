package com.nhnacademy.bookstoreorderapi.order.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

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

    @Builder.Default
    @OneToMany(mappedBy = "wrapping")
    private List<OrderItem> items = new ArrayList<>();

    public void addItem(OrderItem item) {
        this.items.add(item);
        item.setWrapping(this);
    }
}
