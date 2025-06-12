package com.nhnacademy.bookstoreorderapi.order.repository;

import com.nhnacademy.bookstoreorderapi.order.domain.entity.OrderStatusLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OrderStatusLogRepository extends JpaRepository<OrderStatusLog, Long> {
    List<OrderStatusLog> findByOrderId(Long orderId);
}