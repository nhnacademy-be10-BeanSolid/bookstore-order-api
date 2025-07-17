package com.nhnacademy.bookstoreorderapi.common.service;

import com.nhnacademy.bookstoreorderapi.order.domain.Order;
import com.nhnacademy.bookstoreorderapi.payment.domain.entity.Payment;

public interface PointService {
    void processEarnedPoints(Order order, Payment payment);
    void processUsedPoints(Order order, Payment payment);
    void validatePointUsage(Long userNo, Integer usedPoint);
    void processPointRefund(Order order, Long refundAmount); // New method
}
