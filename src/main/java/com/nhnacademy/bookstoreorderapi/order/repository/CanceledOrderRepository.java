package com.nhnacademy.bookstoreorderapi.order.repository;

import com.nhnacademy.bookstoreorderapi.order.domain.entity.CanceledOrder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CanceledOrderRepository extends JpaRepository<CanceledOrder, Long> {
}
