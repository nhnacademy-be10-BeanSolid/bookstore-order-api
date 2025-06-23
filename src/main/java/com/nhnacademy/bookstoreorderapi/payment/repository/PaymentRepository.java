package com.nhnacademy.bookstoreorderapi.payment.repository;

import com.nhnacademy.bookstoreorderapi.payment.domain.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByOrderOrderId(String orderId);
    Optional<Payment> findByPaymentKey(String paymentKey);
}