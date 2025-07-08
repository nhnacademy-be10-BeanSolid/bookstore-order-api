package com.nhnacademy.bookstoreorderapi.order.repository;

import com.nhnacademy.bookstoreorderapi.order.domain.entity.Order;
import com.nhnacademy.bookstoreorderapi.order.domain.entity.OrderStatus;
import com.nhnacademy.bookstoreorderapi.order.domain.entity.ShippingInfo;
import com.nhnacademy.bookstoreorderapi.order.dto.response.OrderSummaryResponse;
import com.nhnacademy.bookstoreorderapi.order.repository.impl.CustomOrderRepositoryImpl;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@DataJpaTest
@Import({com.nhnacademy.bookstoreorderapi.order.common.config.QuerydslConfig.class})
class CustomOrderRepositoryTest {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private JPAQueryFactory jpaQueryFactory;

    private CustomOrderRepository customOrderRepository;

    @BeforeEach
    void setUp() {
        customOrderRepository = new CustomOrderRepositoryImpl(jpaQueryFactory);
        
        // 테스트 데이터 생성
        ShippingInfo shippingInfo1 = new ShippingInfo("홍길동", "01012345678", "서울특별시 강남구", LocalDate.now().plusDays(1), 3000);
        ShippingInfo shippingInfo2 = new ShippingInfo("김철수", "01087654321", "서울특별시 서초구", LocalDate.now().plusDays(2), 3000);
        ShippingInfo shippingInfo3 = new ShippingInfo("이영희", "01055559999", "서울특별시 종로구", LocalDate.now().plusDays(1), 0);
        
        Order order2 = Order.builder()
                .userNo(1L)
                .status(OrderStatus.SHIPPING)
                .orderDate(LocalDate.now().minusDays(1))
                .totalPrice(35000L)
                .shippingInfo(shippingInfo2)
                .build();
        
        Order order1 = Order.builder()
                .userNo(1L)
                .status(OrderStatus.PENDING)
                .orderDate(LocalDate.now())
                .totalPrice(25000L)
                .shippingInfo(shippingInfo1)
                .build();
        
        Order order3 = Order.builder()
                .userNo(2L)
                .status(OrderStatus.COMPLETED)
                .orderDate(LocalDate.now().minusDays(2))
                .totalPrice(45000L)
                .shippingInfo(shippingInfo3)
                .build();
        
        entityManager.persist(order1);
        entityManager.persist(order2);
        entityManager.persist(order3);
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("회원별 주문 요약 조회 - 페이징 적용")
    void findOrderSummary_withPaging_success() {
        // given
        Long userNo = 1L;
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<OrderSummaryResponse> result = customOrderRepository.findOrderSummary(userNo, pageable);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getNumber()).isEqualTo(0);
        assertThat(result.getSize()).isEqualTo(10);
        
        // 최신 주문이 먼저 나와야 함 (createdAt desc)
        OrderSummaryResponse firstOrder = result.getContent().get(0);
        OrderSummaryResponse secondOrder = result.getContent().get(1);
        
        // 실제 데이터베이스에서는 order2가 먼저 persist되어 김철수가 첫 번째로 나옴
        assertThat(firstOrder.receiverName()).isEqualTo("김철수");
        assertThat(secondOrder.receiverName()).isEqualTo("홍길동");
    }

    @Test
    @DisplayName("회원별 주문 요약 조회 - 페이징 제한")
    void findOrderSummary_withLimitedPaging() {
        // given
        Long userNo = 1L;
        Pageable pageable = PageRequest.of(0, 1); // 페이지 크기를 1로 제한

        // when
        Page<OrderSummaryResponse> result = customOrderRepository.findOrderSummary(userNo, pageable);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.hasNext()).isTrue();
        
        OrderSummaryResponse order = result.getContent().get(0);
        assertThat(order.receiverName()).isEqualTo("김철수");
    }

    @Test
    @DisplayName("존재하지 않는 회원의 주문 조회")
    void findOrderSummary_nonExistentUser() {
        // given
        Long userNo = 999L;
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<OrderSummaryResponse> result = customOrderRepository.findOrderSummary(userNo, pageable);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(0);
    }

    @Test
    @DisplayName("다른 회원의 주문은 조회되지 않음")
    void findOrderSummary_differentUser() {
        // given
        Long userNo = 2L;
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<OrderSummaryResponse> result = customOrderRepository.findOrderSummary(userNo, pageable);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
        
        OrderSummaryResponse order = result.getContent().get(0);
        assertThat(order.receiverName()).isEqualTo("이영희");
    }
}