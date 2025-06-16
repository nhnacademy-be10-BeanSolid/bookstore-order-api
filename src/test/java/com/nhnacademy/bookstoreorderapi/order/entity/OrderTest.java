package com.nhnacademy.bookstoreorderapi.order.entity;

import com.nhnacademy.bookstoreorderapi.order.domain.entity.Order;
import com.nhnacademy.bookstoreorderapi.order.domain.entity.OrderItem;
import com.nhnacademy.bookstoreorderapi.order.domain.entity.OrderStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class OrderTest {

    @Test
    void addItem_setsBidirectionalRelation() {
        // given
        Order order = Order.builder()
                .userId("42")
                .status(OrderStatus.PENDING)
                .orderDate(LocalDate.now())
                .requestedDeliveryDate(LocalDate.now().plusDays(2))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .totalPrice(0)
                .deliveryFee(0)
                .build();

        OrderItem item = OrderItem.builder()
                .bookId(100L)
                .quantity(2)
                .unitPrice(10_000)
                .isGiftWrapped(false)
                .build();

        // when
        order.addItem(item);

        // then
        assertThat(order.getItems()).containsExactly(item);  // Order 쪽 컬렉션
        assertThat(item.getOrder()).isEqualTo(order);        // 역방향 연관관계
    }

    @Test
    void builder_initializesItemsList() {
        // when
        Order order = Order.builder().build();

        // then
        assertThat(order.getItems()).isNotNull();
        assertThat(order.getItems()).isEmpty();
    }

    @Test
    void constant_defaultDeliveryFee_is5000() {
        assertThat(Order.DEFAULT_DELIVERY_FEE).isEqualTo(5_000);
    }
}