package com.nhnacademy.bookstoreorderapi.order.repository.impl;

import com.nhnacademy.bookstoreorderapi.order.common.config.QuerydslConfig;
import com.nhnacademy.bookstoreorderapi.order.domain.Order;
import com.nhnacademy.bookstoreorderapi.order.domain.OrderReturn;
import com.nhnacademy.bookstoreorderapi.order.domain.OrderStatus;
import com.nhnacademy.bookstoreorderapi.order.domain.OrderStatusLog;
import com.nhnacademy.bookstoreorderapi.order.repository.OrderRepository;
import com.nhnacademy.bookstoreorderapi.order.repository.OrderStatusLogRepository;
import com.nhnacademy.bookstoreorderapi.payment.domain.PayType;
import com.nhnacademy.bookstoreorderapi.payment.domain.entity.Payment;
import com.nhnacademy.bookstoreorderapi.payment.domain.PaymentStatus;
import com.nhnacademy.bookstoreorderapi.payment.repository.PaymentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@DataJpaTest
@EnableJpaAuditing
@Import(QuerydslConfig.class)
class CustomOrderStatusLogRepositoryImplTest {

    @Autowired
    private TestEntityManager entityManager;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private OrderStatusLogRepository orderStatusLogRepository;
    @Autowired
    private PaymentRepository paymentRepository;

    @Test
    @DisplayName("반품 가능한 주문 - 일반 상품(10일 이내)")
    void canReturnOrder_normalProduct_withinTenDays() {
        // given
        Order order = new Order(1L);
        order.setStatus(OrderStatus.SHIPPING);
        Order savedOrder = orderRepository.save(order);

        OrderStatusLog statusLog = new OrderStatusLog(OrderStatus.PENDING_PAY, OrderStatus.SHIPPING, 1L, "배송 시작", savedOrder);
        orderStatusLogRepository.save(statusLog);

        entityManager.flush();
        entityManager.clear();

        // when
        boolean result = orderStatusLogRepository.canReturnOrder(savedOrder, false);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("반품 불가능한 주문 - 일반 상품(10일 초과)")
    void canReturnOrder_normalProduct_overTenDays() {
        // given
        Order order = new Order(1L);
        order.setStatus(OrderStatus.SHIPPING);
        Order savedOrder = orderRepository.save(order);

        OrderStatusLog statusLog = new OrderStatusLog(OrderStatus.PENDING_PAY, OrderStatus.SHIPPING, 1L, "배송 시작", savedOrder);
        OrderStatusLog savedStatusLog = orderStatusLogRepository.save(statusLog);
        
        // 11일 전으로 직접 업데이트
        entityManager.getEntityManager().createQuery("UPDATE OrderStatusLog o SET o.createdAt = :createdAt WHERE o.id = :id")
                .setParameter("createdAt", LocalDate.now().minusDays(11).atStartOfDay())
                .setParameter("id", savedStatusLog.getId())
                .executeUpdate();

        entityManager.flush();
        entityManager.clear();

        // when
        boolean result = orderStatusLogRepository.canReturnOrder(savedOrder, false);

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("반품 가능한 주문 - 손상된 상품(30일 이내)")
    void canReturnOrder_damagedProduct_withinThirtyDays() {
        // given
        Order order = new Order(1L);
        order.setStatus(OrderStatus.SHIPPING);
        Order savedOrder = orderRepository.save(order);

        OrderStatusLog statusLog = new OrderStatusLog(OrderStatus.PENDING_PAY, OrderStatus.SHIPPING, 1L, "배송 시작", savedOrder);
        
        // 먼저 저장하고 25일 전으로 설정
        OrderStatusLog savedStatusLog = orderStatusLogRepository.save(statusLog);
        
        // 25일 전으로 직접 업데이트
        entityManager.getEntityManager().createQuery("UPDATE OrderStatusLog o SET o.createdAt = :createdAt WHERE o.id = :id")
                .setParameter("createdAt", LocalDate.now().minusDays(25).atStartOfDay())
                .setParameter("id", savedStatusLog.getId())
                .executeUpdate();

        entityManager.flush();
        entityManager.clear();

        // when
        boolean result = orderStatusLogRepository.canReturnOrder(savedOrder, true);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("반품 불가능한 주문 - 손상된 상품(30일 초과)")
    void canReturnOrder_damagedProduct_overThirtyDays() {
        // given
        Order order = new Order(1L);
        order.setStatus(OrderStatus.SHIPPING);
        Order savedOrder = orderRepository.save(order);

        OrderStatusLog statusLog = new OrderStatusLog(OrderStatus.PENDING_PAY, OrderStatus.SHIPPING, 1L, "배송 시작", savedOrder);
        
        // 먼저 저장하고 31일 전으로 설정
        OrderStatusLog savedStatusLog = orderStatusLogRepository.save(statusLog);
        
        // 31일 전으로 직접 업데이트
        entityManager.getEntityManager().createQuery("UPDATE OrderStatusLog o SET o.createdAt = :createdAt WHERE o.id = :id")
                .setParameter("createdAt", LocalDate.now().minusDays(31).atStartOfDay())
                .setParameter("id", savedStatusLog.getId())
                .executeUpdate();

        entityManager.flush();
        entityManager.clear();

        // when
        boolean result = orderStatusLogRepository.canReturnOrder(savedOrder, true);

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("완료된 주문 결제 금액 조회 - 일반 상품 반품(반품 수수료 차감)")
    void getCompletedOrderPaymentAmount_normalProduct_success() {
        // given
        Order order = new Order(1L);
        order.setStatus(OrderStatus.COMPLETED);
        Order savedOrder = orderRepository.save(order);

        OrderStatusLog statusLog = new OrderStatusLog(OrderStatus.SHIPPING, OrderStatus.COMPLETED, 1L, "배송 완료", savedOrder);
        orderStatusLogRepository.save(statusLog);

        Payment payment = Payment.builder()
                .order(savedOrder)
                .paymentKey("test_payment_key")
                .payAmount(15_000L)
                .paymentStatus(PaymentStatus.SUCCESS)
                .payType(PayType.CARD)
                .build();
        paymentRepository.save(payment);

        entityManager.flush();
        entityManager.clear();

        // when
        Optional<Long> result = orderStatusLogRepository.getCompletedOrderPaymentAmount(savedOrder, false);

        // then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(15_000L - OrderReturn.RETURNS_FEE);
    }

    @Test
    @DisplayName("완료된 주문 결제 금액 조회 - 손상된 상품 반품(반품 수수료 없음)")
    void getCompletedOrderPaymentAmount_damagedProduct_success() {
        // given
        Order order = new Order(1L);
        order.setStatus(OrderStatus.COMPLETED);
        Order savedOrder = orderRepository.save(order);

        OrderStatusLog statusLog = new OrderStatusLog(OrderStatus.SHIPPING, OrderStatus.COMPLETED, 1L, "배송 완료", savedOrder);
        orderStatusLogRepository.save(statusLog);

        Payment payment = Payment.builder()
                .order(savedOrder)
                .paymentKey("test_payment_key")
                .payAmount(15_000L)
                .paymentStatus(PaymentStatus.SUCCESS)
                .payType(PayType.CARD)
                .build();
        paymentRepository.save(payment);

        entityManager.flush();
        entityManager.clear();

        // when
        Optional<Long> result = orderStatusLogRepository.getCompletedOrderPaymentAmount(savedOrder, true);

        // then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(15_000L);
    }

    @Test
    @DisplayName("완료된 주문 결제 금액 조회 - 완료 상태 주문은 결제 금액 반환")
    void getCompletedOrderPaymentAmount_completedOrder_returnAmount() {
        // given
        Order order = new Order(1L);
        order.setStatus(OrderStatus.COMPLETED);
        Order savedOrder = orderRepository.save(order);

        OrderStatusLog statusLog = new OrderStatusLog(OrderStatus.SHIPPING, OrderStatus.COMPLETED, 1L, "배송 완료", savedOrder);
        orderStatusLogRepository.save(statusLog);

        Payment payment = Payment.builder()
                .order(savedOrder)
                .paymentKey("test_payment_key")
                .payAmount(15_000L)
                .paymentStatus(PaymentStatus.SUCCESS)
                .payType(PayType.CARD)
                .build();
        paymentRepository.save(payment);

        entityManager.flush();
        entityManager.clear();

        // when
        Optional<Long> result = orderStatusLogRepository.getCompletedOrderPaymentAmount(savedOrder, false);

        // then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(15_000L - OrderReturn.RETURNS_FEE);
    }

    @Test
    @DisplayName("완료된 주문 결제 금액 조회 - 결제 정보가 없는 경우 빈 값 반환")
    void getCompletedOrderPaymentAmount_noPayment_returnEmpty() {
        // given
        Order order = new Order(1L);
        order.setStatus(OrderStatus.RETURNED);
        Order savedOrder = orderRepository.save(order);

        OrderStatusLog statusLog = new OrderStatusLog(OrderStatus.SHIPPING, OrderStatus.COMPLETED, 1L, "배송 완료", savedOrder);
        orderStatusLogRepository.save(statusLog);

        entityManager.flush();
        entityManager.clear();

        // when
        Optional<Long> result = orderStatusLogRepository.getCompletedOrderPaymentAmount(savedOrder, false);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("반품 불가능한 주문 - SHIPPING 상태 로그가 없는 경우")
    void canReturnOrder_noShippingStatusLog_returnFalse() {
        // given
        Order order = new Order(1L);
        order.setStatus(OrderStatus.PENDING_PAY);
        Order savedOrder = orderRepository.save(order);

        OrderStatusLog statusLog = new OrderStatusLog(OrderStatus.PENDING_PAY, OrderStatus.COMPLETED, 1L, "직접 완료", savedOrder);
        orderStatusLogRepository.save(statusLog);

        entityManager.flush();
        entityManager.clear();

        // when
        boolean result = orderStatusLogRepository.canReturnOrder(savedOrder, false);

        // then
        assertThat(result).isFalse();
    }

}