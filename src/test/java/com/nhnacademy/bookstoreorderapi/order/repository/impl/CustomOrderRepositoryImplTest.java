package com.nhnacademy.bookstoreorderapi.order.repository.impl;

import com.nhnacademy.bookstoreorderapi.order.common.config.QuerydslConfig;
import com.nhnacademy.bookstoreorderapi.order.domain.entity.Order;
import com.nhnacademy.bookstoreorderapi.order.domain.entity.OrderStatus;
import com.nhnacademy.bookstoreorderapi.order.domain.entity.ShippingInfo;
import com.nhnacademy.bookstoreorderapi.order.dto.response.OrderSummaryResponse;
import com.nhnacademy.bookstoreorderapi.order.repository.OrderRepository;
import org.aspectj.weaver.ast.Or;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@DataJpaTest
@EnableJpaAuditing
@Import(QuerydslConfig.class)
class CustomOrderRepositoryImplTest {

    @Autowired
    private TestEntityManager entityManager;
    @Autowired
    private OrderRepository orderRepository;

    @Test
    @DisplayName("전체 주문 조회 테스트")
    void shouldFindAllOrders() {
        // given
        Order order1 = new Order(1L);
        order1.setOrderDate(LocalDate.now());
        order1.setShippingInfo(new ShippingInfo("받는이1", "010-1234-5678", "주소", LocalDate.now(), 500));
        order1.setTotalPrice(10_000L);
        order1.setStatus(OrderStatus.SHIPPING);

        Order order2 = new Order(2L);
        order2.setOrderDate(LocalDate.now().minusDays(1));
        order2.setShippingInfo(new ShippingInfo("받는이2", "010-1234-5678", "주소", LocalDate.now(), 500));
        order2.setTotalPrice(5_000L);
        order2.setStatus(OrderStatus.PENDING);

        orderRepository.saveAll(List.of(order1, order2));

        entityManager.flush();
        entityManager.clear();

        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<OrderSummaryResponse> result = orderRepository.findAllOrderSummary(pageable);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getTotalPages()).isEqualTo(1);
    }

    @Test
    @DisplayName("주문번호로 주문ID 조회 테스트")
    void shouldFindOrderId() {
        // given
        Order order = new Order(1L);
        Order saved = orderRepository.save(order);

        // when
        Long result = orderRepository.findIdByOrderNumber(saved.getOrderNumber());

        // then
        assertThat(result).isEqualTo(saved.getId());
    }

    @Test
    @DisplayName("주문ID로 주문번호 조회 테스트")
    void shouldFindOrderNumber() {
        // given
        Order order = new Order(1L);
        Order saved = orderRepository.save(order);

        // when
        String result = orderRepository.findOrderNumberById(saved.getId());

        // then
        assertThat(result).containsPattern("^\\d{6}-\\w{6}-\\w{6}$");
    }
}