package com.nhnacademy.bookstoreorderapi.order.repository;

import com.nhnacademy.bookstoreorderapi.order.domain.entity.Order;

import java.util.Optional;

public interface CustomOrderStatusLogRepository {

    boolean canReturnOrder(Order order, boolean damaged);
    Optional<Long> getCompletedOrderPaymentAmount(Order order);
}
