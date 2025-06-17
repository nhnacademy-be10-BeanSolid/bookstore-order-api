package com.nhnacademy.bookstoreorderapi.payment.service;

import com.nhnacademy.bookstoreorderapi.payment.domain.entity.Payment;

public interface PaymentService {
    Payment saveInitial(Payment payment, String userEmail);

    void markSuccess(String paymentKey, Long orderId, long amount);
    void markFail(Long orderId, String failMessage);
}