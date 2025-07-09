package com.nhnacademy.bookstoreorderapi.order.service.impl;

import com.nhnacademy.bookstoreorderapi.order.dto.response.UserOrderAmountResponse;
import com.nhnacademy.bookstoreorderapi.order.repository.CustomOrderRepository;
import com.nhnacademy.bookstoreorderapi.order.service.OrderInternalService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderInternalServiceImpl implements OrderInternalService {

    private final CustomOrderRepository customOrderRepository;

    //TODO: RabbitMQ 사용해서 리팩토링할 예정
    @Scheduled(cron = "0 0 0 1 * ?")
    @Override
    public List<UserOrderAmountResponse> findOrderAmountGroupByUserLastThreeMonths() {
        return customOrderRepository.findOrderAmountGroupByUserLastThreeMonths();
    }
}
