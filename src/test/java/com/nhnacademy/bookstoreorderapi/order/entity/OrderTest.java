package com.nhnacademy.bookstoreorderapi.order.entity;

import com.nhnacademy.bookstoreorderapi.order.domain.entity.Order;
import com.nhnacademy.bookstoreorderapi.order.domain.entity.OrderItem;
import com.nhnacademy.bookstoreorderapi.order.domain.entity.OrderStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class OrderTest {

    @Test
    @DisplayName("Builder should correctly assign all fields")
    void builder_assignsAllFields() {
        LocalDateTime now = LocalDateTime.now();
        LocalDate desiredDate = LocalDate.of(2025, 6, 15);

        Order order = Order.builder()
                .id(100L)
                .userId(42L)
                .guestName("John Doe")
                .guestPhone("010-1234-5678")
                .status(OrderStatus.PENDING)
                .requestedAt(now)
                .deliveryAt(now.plusDays(3))
                .createdAt(now.minusDays(1))
                .updatedAt(now)
                .totalPrice(20000)
                .deliveryFee(3000)
                .finalPrice(23000)
                .build();

        assertThat(order.getId()).isEqualTo(100L);
        assertThat(order.getUserId()).isEqualTo(42L);
        assertThat(order.getGuestName()).isEqualTo("John Doe");
        assertThat(order.getGuestPhone()).isEqualTo("010-1234-5678");
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(order.getRequestedAt()).isSameAs(now);
        assertThat(order.getDeliveryAt()).isEqualTo(now.plusDays(3));
        assertThat(order.getCreatedAt()).isEqualTo(now.minusDays(1));
        assertThat(order.getUpdatedAt()).isSameAs(now);
        assertThat(order.getTotalPrice()).isEqualTo(20000);
        assertThat(order.getDeliveryFee()).isEqualTo(3000);
        assertThat(order.getFinalPrice()).isEqualTo(23000);
        assertThat(order.getItems()).isEmpty();
    }

    @Test
    @DisplayName("addItem should set order reference and add to items list")
    void addItem_setsOrderAndAddsToList() {
        Order order = Order.builder().id(1L).build();
        OrderItem item = new OrderItem();
        item.setId(10L);
//        item.setProductId(5L);
        item.setQuantity(2);
        item.setUnitPrice(1500);

        order.addItem(item);

        assertThat(order.getItems()).containsExactly(item);
        assertThat(item.getOrder()).isSameAs(order);
    }
}