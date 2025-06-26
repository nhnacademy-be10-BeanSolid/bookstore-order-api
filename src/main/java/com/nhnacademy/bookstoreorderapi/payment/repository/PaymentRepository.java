package com.nhnacademy.bookstoreorderapi.payment.repository;

import com.nhnacademy.bookstoreorderapi.order.domain.entity.Order;
import com.nhnacademy.bookstoreorderapi.payment.domain.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    // 주문 엔티티(FK)로 결제 1건 조회
    Optional<Payment> findByOrder(Order order);

    //Toss paymentKey 로 조회
    Optional<Payment> findByPaymentKey(String paymentKey);
}