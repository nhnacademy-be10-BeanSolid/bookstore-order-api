//package com.nhnacademy.bookstoreorderapi.order.repository;
//
//import com.nhnacademy.bookstoreorderapi.order.domain.entity.Order;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
//import org.springframework.test.context.ActiveProfiles;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Optional;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//@ActiveProfiles("test")
//@DataJpaTest
//class OrderRepositoryTest {
//
//    List<Order> orders = new ArrayList<>();
//
//    @Autowired
//    private OrderRepository orderRepository;
//
//    @BeforeEach
//    void setUp() {
//
//        Order memberOrder1 = Order.builder().userNo(1L).totalPrice(10_000L).build();
//        Order memberOrder2 = Order.builder().userNo(1L).totalPrice(5_000L).build();
//        Order guestOrder1 = Order.builder().totalPrice(7_000L).build();
//        Order guestOrder2 = Order.builder().totalPrice(3_000L).build();
//
//        orders.addAll(List.of(memberOrder1, memberOrder2, guestOrder1, guestOrder2));
//    }
//
//    @Test
//    @DisplayName("주문 데이터 저장 및 조회 테스트")
//    void saveAndFindById() {
//
//        List<Order> saved = orderRepository.saveAll(orders);
//        Optional<Order> found = orderRepository.findById(saved.getFirst().getId());
//
//        assertThat(found).isPresent();
//        assertThat(found.get().getId()).isNotNull();
//        assertThat(found.get().getOrderId()).containsPattern("^[0-9]{6}-[A-Za-z0-9]{6}-[A-Za-z0-9]{6}$");
//    }
//
//    @Test
//    @DisplayName("회원의 전체 주문 조회 테스트")
//    void findAllByUserNo() {
//
//        orderRepository.saveAll(orders);
//        List<Order> orderList = orderRepository.findAllByUserNo(1L);
//
//        assertThat(orderList).isNotEmpty();
//        assertThat(orderList).allMatch(order -> order.getUserNo() == 1L);
//        assertThat(orderList).extracting(Order::getTotalPrice)
//                        .containsExactlyInAnyOrder(10_000L, 5_000L);
//    }
//}
