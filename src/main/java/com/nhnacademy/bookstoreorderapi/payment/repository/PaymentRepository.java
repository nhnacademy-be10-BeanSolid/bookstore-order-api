package com.nhnacademy.bookstoreorderapi.payment.repository;

import com.nhnacademy.bookstoreorderapi.payment.domain.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    // orderId 컬럼으로 바로 조회 – OK
    Optional<Payment> findByOrderId(String orderId);


    // paymentKey 컬럼으로 조회 – OK
    Optional<Payment> findByPaymentKey(String paymentKey);
}