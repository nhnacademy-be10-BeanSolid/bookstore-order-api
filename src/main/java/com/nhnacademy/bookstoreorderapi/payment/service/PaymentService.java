package com.nhnacademy.bookstoreorderapi.payment.service;

import com.nhnacademy.bookstoreorderapi.payment.domain.entity.Payment;

import java.util.Map;

public interface PaymentService {

    Payment saveInitial(Payment payment, String userId);




    void markSuccess(String paymentKey, String orderId, long amount);


    void markFail(String paymentKey, String failMessage);


    Map<String, Object> cancelPaymentPoint(String paymentKey,
                                           String cancelReason,
                                           String userId,
                                           Long guestId);
}