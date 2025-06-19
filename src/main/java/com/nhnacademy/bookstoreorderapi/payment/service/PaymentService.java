package com.nhnacademy.bookstoreorderapi.payment.service;

import com.nhnacademy.bookstoreorderapi.payment.domain.entity.Payment;

public interface PaymentService {
    Payment saveInitial(Payment payment, String userEmail);

    void markSuccess(String paymentKey, String orderId, long amount);
    void markFail(String orderId, String failMessage);
}