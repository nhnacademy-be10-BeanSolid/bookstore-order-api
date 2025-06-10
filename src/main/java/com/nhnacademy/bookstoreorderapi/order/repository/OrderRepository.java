package com.nhnacademy.bookstoreorderapi.order.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.nhnacademy.bookstoreorderapi.order.domain.entity.Order;

// JpaRepository<Order, Long> 를 상속해야 save() 메서드를 쓸 수 있습니다.
public interface OrderRepository extends JpaRepository<Order, Long> { }