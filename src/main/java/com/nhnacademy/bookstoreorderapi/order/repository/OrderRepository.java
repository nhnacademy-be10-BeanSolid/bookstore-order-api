package com.nhnacademy.bookstoreorderapi.order.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.nhnacademy.bookstoreorderapi.order.domain.entity.Order;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findAllByUserId(Long userId);
//    boolean existsByOrderId(String orderId);
    //Order 객체를 곧바로 넘겨야 해서 findByOrderId()로 선택함... 사실 existsByOrderId가 가벼워서 고민 많이함

    /*OrderfindByOrderId()로 하면 뭐리 수를 1회로 최소화,
    트랜잭션 내에서 영속 객체를 유지해 성능, 일관성을 챙김
     */

    Order findByOrderId(String orderId);
}