package com.nhnacademy.bookstoreorderapi.order.repository;

import com.nhnacademy.bookstoreorderapi.order.domain.entity.Order;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@DataJpaTest
class OrderRepositoryTest {

    @Autowired
    private OrderRepository orderRepository;

    @Test
    @DisplayName("주문 데이터 저장 및 조회 테스트")
    void saveAndFindById() {

        Order order = Order.builder()
                .orderId("testNumber")
                .totalPrice(10_000)
                .build();

        Order saved = orderRepository.save(order);
        Optional<Order> found = orderRepository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getOrderId()).isEqualTo("testNumber");
        assertThat(found.get().getTotalPrice()).isEqualTo(10_000);
    }
}
