package com.nhnacademy.bookstoreorderapi.order.repository.impl;

import com.nhnacademy.bookstoreorderapi.order.common.config.QuerydslConfig;
import com.nhnacademy.bookstoreorderapi.order.domain.Order;
import com.nhnacademy.bookstoreorderapi.order.domain.OrderItem;
import com.nhnacademy.bookstoreorderapi.order.domain.OrderStatus;
import com.nhnacademy.bookstoreorderapi.order.domain.ShippingInfo;
import com.nhnacademy.bookstoreorderapi.order.dto.request.UpdateOrderRequest;
import com.nhnacademy.bookstoreorderapi.order.dto.response.OrderSummaryResponse;
import com.nhnacademy.bookstoreorderapi.order.dto.response.UserOrderAmountResponse;
import com.nhnacademy.bookstoreorderapi.order.repository.OrderRepository;
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

    private final ShippingInfo shippingInfo = new ShippingInfo(
            new UpdateOrderRequest(null, "받는 사람", "010-1234-5678", "주소", LocalDate.now().plusDays(1)),
            ShippingInfo.DEFAULT_SHIPPING_FEE);

    @Test
    @DisplayName("전체 주문 조회 테스트")
    void shouldFindAllOrders() {
        // given
        Order order1 = new Order(1L);
        order1.setOrderDate(LocalDate.now());
        order1.setShippingInfo(shippingInfo);
        order1.setTotalPrice(10_000L);
        order1.setStatus(OrderStatus.SHIPPING);

        Order order2 = new Order(2L);
        order2.setOrderDate(LocalDate.now().minusDays(1));
        order2.setShippingInfo(shippingInfo);
        order2.setTotalPrice(5_000L);
        order2.setStatus(OrderStatus.PENDING_PAY);

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

    @Test
    @DisplayName("특정 회원의 주문 조회 테스트")
    void shouldFindOrderSummaryByUserNo() {
        // given
        Long userNo = 1L;
        Order order1 = new Order(userNo);
        order1.setOrderDate(LocalDate.now());
        order1.setShippingInfo(shippingInfo);
        order1.setTotalPrice(10_000L);
        order1.setStatus(OrderStatus.SHIPPING);

        Order order2 = new Order(userNo);
        order2.setOrderDate(LocalDate.now().minusDays(1));
        order2.setShippingInfo(shippingInfo);
        order2.setTotalPrice(5_000L);
        order2.setStatus(OrderStatus.PENDING_PAY);

        Order order3 = new Order(2L); // 다른 사용자의 주문
        order3.setOrderDate(LocalDate.now());
        order3.setShippingInfo(shippingInfo);
        order3.setTotalPrice(3_000L);
        order3.setStatus(OrderStatus.COMPLETED);

        orderRepository.saveAll(List.of(order1, order2, order3));

        entityManager.flush();
        entityManager.clear();

        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<OrderSummaryResponse> result = orderRepository.findOrderSummary(userNo, pageable);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent()).hasSize(2);
    }

    @Test
    @DisplayName("최근 3개월 회원별 주문 금액 조회 테스트")
    void shouldFindOrderAmountGroupByUserLastThreeMonths() {
        // given
        Long userNo1 = 1L;
        Long userNo2 = 2L;

        Order order1 = new Order(userNo1);
        order1.setOrderDate(LocalDate.now().minusMonths(1));
        order1.setStatus(OrderStatus.COMPLETED);
        Order savedOrder1 = orderRepository.save(order1);

        Order order2 = new Order(userNo2);
        order2.setOrderDate(LocalDate.now().minusMonths(2));
        order2.setStatus(OrderStatus.SHIPPING);
        Order savedOrder2 = orderRepository.save(order2);

        Order order3 = new Order(userNo1);
        order3.setOrderDate(LocalDate.now().minusMonths(4)); // 3개월 이전
        order3.setStatus(OrderStatus.COMPLETED);
        Order savedOrder3 = orderRepository.save(order3);

        OrderItem orderItem1 = new OrderItem(1L, "책1", 10_000, 2, savedOrder1);
        OrderItem orderItem2 = new OrderItem(2L, "책2", 5_000, 1, savedOrder2);
        OrderItem orderItem3 = new OrderItem(3L, "책3", 3_000, 3, savedOrder3);

        entityManager.persist(orderItem1);
        entityManager.persist(orderItem2);
        entityManager.persist(orderItem3);

        entityManager.flush();
        entityManager.clear();

        // when
        List<UserOrderAmountResponse> result = orderRepository.findOrderAmountGroupByUserLastThreeMonths();

        // then
        assertThat(result).hasSize(2);
        assertThat(result).extracting("userNo").containsExactlyInAnyOrder(userNo1, userNo2);
        
        UserOrderAmountResponse user1Result = result.stream()
                .filter(r -> r.userNo().equals(userNo1))
                .findFirst()
                .orElseThrow();
        assertThat(user1Result.pureOrderAmount()).isEqualTo(20_000L); // 10_000 * 2

        UserOrderAmountResponse user2Result = result.stream()
                .filter(r -> r.userNo().equals(userNo2))
                .findFirst()
                .orElseThrow();
        assertThat(user2Result.pureOrderAmount()).isEqualTo(5_000L); // 5_000 * 1
    }

    @Test
    @DisplayName("구매 검증 테스트 - 구매한 경우")
    void shouldVerifyPurchase_purchased() {
        // given
        Long userNo = 1L;
        Long bookId = 100L;

        Order order = new Order(userNo);
        order.setStatus(OrderStatus.COMPLETED);
        Order savedOrder = orderRepository.save(order);

        OrderItem orderItem = new OrderItem(bookId, "책제목", 10_000, 1, savedOrder);
        entityManager.persist(orderItem);

        entityManager.flush();
        entityManager.clear();

        // when
        boolean result = orderRepository.findByUserNoAndBookId(userNo, bookId);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("구매 검증 테스트 - 구매하지 않은 경우")
    void shouldVerifyPurchase_notPurchased() {
        // given
        Long userNo = 1L;
        Long bookId = 100L;

        // when
        boolean result = orderRepository.findByUserNoAndBookId(userNo, bookId);

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("존재하지 않는 주문번호로 주문ID 조회 테스트")
    void shouldReturnNullForNonExistentOrderNumber() {
        // given
        String nonExistentOrderNumber = "999999-abc123-def456";

        // when
        Long result = orderRepository.findIdByOrderNumber(nonExistentOrderNumber);

        // then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("존재하지 않는 주문ID로 주문번호 조회 테스트")
    void shouldReturnNullForNonExistentOrderId() {
        // given
        Long nonExistentOrderId = 999999L;

        // when
        String result = orderRepository.findOrderNumberById(nonExistentOrderId);

        // then
        assertThat(result).isNull();
    }
}