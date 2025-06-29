package com.nhnacademy.bookstoreorderapi.order.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.nhnacademy.bookstoreorderapi.order.domain.entity.Order;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findAllByUserNo(Long userNo);
    Order findByOrderId(String orderId);
    Optional<Order> findByOrderIdAndUserNo(String orderId, Long userNo);
}