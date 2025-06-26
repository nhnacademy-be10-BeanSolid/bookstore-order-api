package com.nhnacademy.bookstoreorderapi.order.repository;

import com.nhnacademy.bookstoreorderapi.order.domain.entity.Order;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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
                .totalPrice(10_000L)
                .build();

        Order saved = orderRepository.save(order);
        Optional<Order> found = orderRepository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getId()).isNotNull();
        assertThat(found.get().getOrderId()).containsPattern("^[0-9]{6}-[A-Za-z0-9]{6}-[A-Za-z0-9]{6}$");
        assertThat(found.get().getTotalPrice()).isEqualTo(10_000L);
    }

    @Test
    @DisplayName("회원의 전체 주문 조회 테스트")
    void findAllByUserId() {

        Order order1 = Order.builder().userId(1L).totalPrice(10_000L).build();
        Order order2 = Order.builder().userId(1L).totalPrice(5_000L).build();

        orderRepository.saveAll(List.of(order1, order2));
        List<Order> orderList = orderRepository.findAllByUserId(1L);

        assertThat(orderList).isNotEmpty();
        assertThat(orderList).allMatch(order -> order.getUserId() == 1L);
        assertThat(orderList.getFirst().getTotalPrice()).isEqualTo(10_000L);
        assertThat(orderList.get(1).getTotalPrice()).isEqualTo(5_000L);
    }
}
