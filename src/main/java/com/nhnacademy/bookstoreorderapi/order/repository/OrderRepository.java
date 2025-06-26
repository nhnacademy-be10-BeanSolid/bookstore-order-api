package com.nhnacademy.bookstoreorderapi.order.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.nhnacademy.bookstoreorderapi.order.domain.entity.Order;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findAllByUserId(Long userId);
}