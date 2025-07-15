package com.nhnacademy.bookstoreorderapi.order.repository;

import com.nhnacademy.bookstoreorderapi.order.common.config.QuerydslConfig;
import com.nhnacademy.bookstoreorderapi.order.domain.entity.Order;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@DataJpaTest
@EnableJpaAuditing
@Import(QuerydslConfig.class)
class OrderRepositoryTest {

    @Autowired
    private OrderRepository orderRepository;

    @Test
    @DisplayName("주문 데이터 저장 테스트('구매하기' 버튼을 눌렀을 때)")
    void shouldSaveOrder() {
        // given
        Order order = new Order(1L);

        // when
        Order saved = orderRepository.save(order);

        // then
        assertThat(saved.getOrderNumber()).isNotNull();
        assertThat(saved.getOrderNumber()).containsPattern("^\\d{6}-\\w{6}-\\w{6}$");
    }

    @Test
    @DisplayName("주문 데이터 조회 테스트('완료되지 않은 주문' 조회)")
    void shouldFindOrder() {
        // given
        Long userNo = 1L;
        Order order = new Order(userNo);

        // when
        Order saved = orderRepository.save(order);
        Optional<Order> found = orderRepository.findByOrderNumberAndUserNo(saved.getOrderNumber(), userNo);

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getOrderNumber()).isEqualTo(saved.getOrderNumber());
    }

    @Test
    @DisplayName("주문 데이터 수정 테스트")
    void shouldUpdateOrder() {
        // given
        Long userNo = 1L;
        Order order = new Order(1L);
        Order saved = orderRepository.save(order);

        // when
        Order found = orderRepository.findByOrderNumberAndUserNo(saved.getOrderNumber(), userNo)
                .orElseThrow();
        found.setTotalPrice(10_000L);

        // then
        Order result = orderRepository.findByOrderNumberAndUserNo(found.getOrderNumber(), userNo)
                .orElseThrow();
        assertThat(result.getTotalPrice()).isEqualTo(10_000L);
    }
}
