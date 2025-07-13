package com.nhnacademy.bookstoreorderapi.order.repository;

import com.nhnacademy.bookstoreorderapi.order.domain.entity.Order;
import org.aspectj.weaver.ast.Or;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@DataJpaTest
@EnableJpaAuditing
class OrderRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;
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
//
//    @Test
//    @DisplayName("주문 데이터 저장 및 조회 테스트")
//    void saveAndFindById() {
//        List<Order> saved = orderRepository.saveAll(orders);
//        Optional<Order> found = orderRepository.findById(saved.getFirst().getId());
//
//        assertThat(found).isPresent();
//        assertThat(found.get().getId()).isNotNull();
//        assertThat(found.get().getOrderNumber()).containsPattern("^[0-9]{6}-[A-Za-z0-9]{6}-[A-Za-z0-9]{6}$");
//    }
//
//    @Test
//    @DisplayName("주문 ID로 주문 조회 테스트")
//    void findByOrderId() {
//        List<Order> saved = orderRepository.saveAll(orders);
//        Order savedOrder = saved.getFirst();
//
//        Optional<Order> found = orderRepository.findByOrderId(savedOrder.getOrderNumber());
//
//        assertThat(found).isPresent();
//        assertThat(found.get().getId()).isEqualTo(savedOrder.getId());
//        assertThat(found.get().getOrderNumber()).isEqualTo(savedOrder.getOrderNumber());
//    }
//
//    @Test
//    @DisplayName("존재하지 않는 주문 ID로 조회 시 빈 Optional 반환")
//    void findByOrderId_notFound() {
//        Optional<Order> found = orderRepository.findByOrderId("NON-EXISTENT-ORDER");
//
//        assertThat(found).isEmpty();
//    }
//
//    @Test
//    @DisplayName("회원 주문 상세 조회 테스트")
//    void findByOrderIdAndUserNo() {
//        List<Order> saved = orderRepository.saveAll(orders);
//        Order memberOrder = saved.stream()
//                .filter(order -> order.getUserNo() != null && order.getUserNo().equals(1L))
//                .findFirst()
//                .orElseThrow();
//
//        Optional<Order> found = orderRepository.findByOrderIdAndUserNo(memberOrder.getOrderNumber(), 1L);
//
//        assertThat(found).isPresent();
//        assertThat(found.get().getId()).isEqualTo(memberOrder.getId());
//        assertThat(found.get().getUserNo()).isEqualTo(1L);
//    }
//
//    @Test
//    @DisplayName("다른 회원의 주문 조회 시 빈 Optional 반환")
//    void findByOrderIdAndUserNo_differentUser() {
//        List<Order> saved = orderRepository.saveAll(orders);
//        Order memberOrder = saved.stream()
//                .filter(order -> order.getUserNo() != null && order.getUserNo().equals(1L))
//                .findFirst()
//                .orElseThrow();
//
//        Optional<Order> found = orderRepository.findByOrderIdAndUserNo(memberOrder.getOrderNumber(), 2L);
//
//        assertThat(found).isEmpty();
//    }
//
//    @Test
//    @DisplayName("비회원 주문은 회원 주문 조회로 찾을 수 없음")
//    void findByOrderIdAndUserNo_guestOrder() {
//        List<Order> saved = orderRepository.saveAll(orders);
//        Order guestOrder = saved.stream()
//                .filter(order -> order.getUserNo() == null)
//                .findFirst()
//                .orElseThrow();
//
//        Optional<Order> found = orderRepository.findByOrderIdAndUserNo(guestOrder.getOrderNumber(), 1L);
//
//        assertThat(found).isEmpty();
//    }
//
//    @Test
//    @DisplayName("존재하지 않는 주문 ID로 회원 주문 조회 시 빈 Optional 반환")
//    void findByOrderIdAndUserNo_notFound() {
//        Optional<Order> found = orderRepository.findByOrderIdAndUserNo("NON-EXISTENT-ORDER", 1L);
//
//        assertThat(found).isEmpty();
//    }
}
