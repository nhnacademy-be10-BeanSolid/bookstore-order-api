package com.nhnacademy.bookstoreorderapi.payment.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import com.nhnacademy.bookstoreorderapi.payment.exception.PaymentNotFoundException;
import com.nhnacademy.bookstoreorderapi.payment.gateway.PaymentGateway;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import com.nhnacademy.bookstoreorderapi.payment.domain.*;
import com.nhnacademy.bookstoreorderapi.payment.domain.entity.Payment;
import com.nhnacademy.bookstoreorderapi.payment.repository.PaymentRepository;
import com.nhnacademy.bookstoreorderapi.payment.service.PaymentService;

/**
 * 결제 비즈니스 로직.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentGateway paymentGateway;   // Toss·Payco 등 구현체 주입

    @Override
    public Payment createPayment(Long orderId,
                                 PaymentMethod method,
                                 String provider,
                                 int amount) {

        Payment payment = Payment.builder()
                .orderId(orderId)
                .method(method)
                .provider(provider)
                .amount(amount)
                .status(PaymentStatus.REQUESTED)
                .createdAt(LocalDateTime.now())
                .build();

        paymentGateway.requestPayment(payment);   // PG 호출
        return paymentRepository.save(payment);
    }

    @Override
    public Payment approvePayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException(paymentId));

        payment.approve(LocalDateTime.now());
        paymentGateway.confirmPayment(payment);
        return payment;
    }

    @Override
    public Payment cancelPayment(Long paymentId, int cancelAmount, String reason) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException(paymentId));

        payment.cancel(cancelAmount, reason, LocalDateTime.now());
        paymentGateway.cancelPayment(payment);
        return payment;
    }


    @Transactional(readOnly = true)
    public Payment getPayment(Long paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException(paymentId));
    }

    @Transactional(readOnly = true)
    public List<Payment> getPaymentsByOrder(Long orderId) {
        return paymentRepository.findAllByOrderId(orderId);
    }

    @Transactional(readOnly = true)
    public List<Payment> getPaymentsByStatus(PaymentStatus status) {
        return paymentRepository.findAllByStatus(status);
    }
}