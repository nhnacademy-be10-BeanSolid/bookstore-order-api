package com.nhnacademy.bookstoreorderapi.payment.repository;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nhnacademy.bookstoreorderapi.payment.domain.entity.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Long> {


    Optional<Payment> findByOrderId(Long orderId);
    Optional<Payment> findByPaymentKey(String paymentKey);
}