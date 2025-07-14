package com.nhnacademy.bookstoreorderapi.order.repository;

import com.nhnacademy.bookstoreorderapi.order.dto.response.OrderSummaryResponse;
import com.nhnacademy.bookstoreorderapi.order.dto.response.PurchaseVerificationResponse;
import com.nhnacademy.bookstoreorderapi.order.dto.response.UserOrderAmountResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CustomOrderRepository {

    Page<OrderSummaryResponse> findAllOrderSummary(Pageable pageable);
    Page<OrderSummaryResponse> findOrderSummary(Long userNo, Pageable pageable);
    List<UserOrderAmountResponse> findOrderAmountGroupByUserLastThreeMonths();
    PurchaseVerificationResponse findByUserNoAndBookId(Long userNo, Long bookId);
}
