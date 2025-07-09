package com.nhnacademy.bookstoreorderapi.order.service;

import com.nhnacademy.bookstoreorderapi.order.dto.response.UserOrderAmountResponse;

import java.util.List;

public interface OrderInternalService {

    List<UserOrderAmountResponse> findOrderAmountGroupByUserLastThreeMonths();
}
