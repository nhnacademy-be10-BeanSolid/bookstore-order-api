package com.nhnacademy.bookstoreorderapi.order.service.impl;

import com.nhnacademy.bookstoreorderapi.order.domain.entity.Order;
import com.nhnacademy.bookstoreorderapi.order.dto.response.UserOrderAmountResponse;
import com.nhnacademy.bookstoreorderapi.order.exception.notfound.OrderNotFoundException;
import com.nhnacademy.bookstoreorderapi.order.repository.OrderRepository;
import com.nhnacademy.bookstoreorderapi.order.service.OrderInternalService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderInternalServiceImpl implements OrderInternalService {

    private final OrderRepository orderRepository;

    @Scheduled(cron = "0 0 0 1 * ?")
    @Transactional(readOnly = true)
    @Override
    public List<UserOrderAmountResponse> findOrderAmountGroupByUserLastThreeMonths() {
        return orderRepository.findOrderAmountGroupByUserLastThreeMonths();
    }

    @Override
    public Long findIdByOrderNumber(String orderNumber) {
        return orderRepository.findIdByOrderNumber(orderNumber);
    }

    @Override
    public String findOrderNumberById(Long orderId) {
        Order order = orderRepository.findOrderNumberById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("" + orderId + "가 없습니다"));

        return order.getOrderNumber();
    }
}
