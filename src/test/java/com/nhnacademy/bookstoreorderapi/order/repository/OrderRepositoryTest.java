package com.nhnacademy.bookstoreorderapi.order.repository;

import com.nhnacademy.bookstoreorderapi.order.domain.entity.Order;
import com.nhnacademy.bookstoreorderapi.order.domain.entity.OrderStatus;
import com.nhnacademy.bookstoreorderapi.order.domain.entity.ShippingInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@DataJpaTest
class OrderRepositoryTest {

    @Autowired
    private OrderRepository orderRepository;

    private List<Order> orders = new ArrayList<>();

    @BeforeEach
    void setUp() {
        ShippingInfo shippingInfo = new ShippingInfo(
                "홍길동",
                "01012345678",
                "서울특별시 강남구 테헤란로 123",
                LocalDate.now().plusDays(3),
                3000
        );

        Order memberOrder1 = Order.builder()
                .userNo(1L)
                .totalPrice(10_000L)
                .status(OrderStatus.PENDING)
                .shippingInfo(shippingInfo)
                .build();

        Order memberOrder2 = Order.builder()
                .userNo(1L)
                .totalPrice(5_000L)
                .status(OrderStatus.PENDING)
                .shippingInfo(shippingInfo)
                .build();

        Order guestOrder1 = Order.builder()
                .totalPrice(7_000L)
                .status(OrderStatus.PENDING)
                .shippingInfo(shippingInfo)
                .build();

        Order guestOrder2 = Order.builder()
                .totalPrice(3_000L)
                .status(OrderStatus.PENDING)
                .shippingInfo(shippingInfo)
                .build();

        orders.addAll(List.of(memberOrder1, memberOrder2, guestOrder1, guestOrder2));
    }

    @Test
    @DisplayName("주문 데이터 저장 및 조회 테스트")
    void saveAndFindById() {
        List<Order> saved = orderRepository.saveAll(orders);
        Optional<Order> found = orderRepository.findById(saved.getFirst().getId());

        assertThat(found).isPresent();
        assertThat(found.get().getId()).isNotNull();
        assertThat(found.get().getOrderId()).containsPattern("^[0-9]{6}-[A-Za-z0-9]{6}-[A-Za-z0-9]{6}$");
    }

    @Test
    @DisplayName("주문 ID로 주문 조회 테스트")
    void findByOrderId() {
        List<Order> saved = orderRepository.saveAll(orders);
        Order savedOrder = saved.getFirst();
        
        Optional<Order> found = orderRepository.findByOrderId(savedOrder.getOrderId());
        
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(savedOrder.getId());
        assertThat(found.get().getOrderId()).isEqualTo(savedOrder.getOrderId());
    }

    @Test
    @DisplayName("존재하지 않는 주문 ID로 조회 시 빈 Optional 반환")
    void findByOrderId_notFound() {
        Optional<Order> found = orderRepository.findByOrderId("NON-EXISTENT-ORDER");
        
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("회원 주문 상세 조회 테스트")
    void findByOrderIdAndUserNo() {
        List<Order> saved = orderRepository.saveAll(orders);
        Order memberOrder = saved.stream()
                .filter(order -> order.getUserNo() != null && order.getUserNo().equals(1L))
                .findFirst()
                .orElseThrow();
        
        Optional<Order> found = orderRepository.findByOrderIdAndUserNo(memberOrder.getOrderId(), 1L);
        
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(memberOrder.getId());
        assertThat(found.get().getUserNo()).isEqualTo(1L);
    }

    @Test
    @DisplayName("다른 회원의 주문 조회 시 빈 Optional 반환")
    void findByOrderIdAndUserNo_differentUser() {
        List<Order> saved = orderRepository.saveAll(orders);
        Order memberOrder = saved.stream()
                .filter(order -> order.getUserNo() != null && order.getUserNo().equals(1L))
                .findFirst()
                .orElseThrow();
        
        Optional<Order> found = orderRepository.findByOrderIdAndUserNo(memberOrder.getOrderId(), 2L);
        
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("비회원 주문은 회원 주문 조회로 찾을 수 없음")
    void findByOrderIdAndUserNo_guestOrder() {
        List<Order> saved = orderRepository.saveAll(orders);
        Order guestOrder = saved.stream()
                .filter(order -> order.getUserNo() == null)
                .findFirst()
                .orElseThrow();
        
        Optional<Order> found = orderRepository.findByOrderIdAndUserNo(guestOrder.getOrderId(), 1L);
        
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("존재하지 않는 주문 ID로 회원 주문 조회 시 빈 Optional 반환")
    void findByOrderIdAndUserNo_notFound() {
        Optional<Order> found = orderRepository.findByOrderIdAndUserNo("NON-EXISTENT-ORDER", 1L);
        
        assertThat(found).isEmpty();
    }
}
