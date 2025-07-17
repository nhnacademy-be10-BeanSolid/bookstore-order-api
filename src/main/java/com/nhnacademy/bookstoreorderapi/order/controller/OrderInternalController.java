package com.nhnacademy.bookstoreorderapi.order.controller;

import com.nhnacademy.bookstoreorderapi.order.dto.request.ValidatePurchaseRequest;
import com.nhnacademy.bookstoreorderapi.order.dto.response.UserOrderAmountResponse;
import com.nhnacademy.bookstoreorderapi.order.service.OrderInternalService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 내부 서비스 간 통신할 때 쓰는 api를 모아놓은 클래스입니다.
 */
@RestController
@RequestMapping("/internal/orders")
@RequiredArgsConstructor
public class OrderInternalController {

    private final OrderInternalService orderInternalService;

    // READ (조회)
    // 최근 3개월 순수 주문금액 조회
    @GetMapping
    public List<UserOrderAmountResponse> getOrderAmountGroupByUserLastThreeMonth() {
        return orderInternalService.findOrderAmountGroupByUserLastThreeMonths();
    }

    // 주문번호로 주문ID 조회 (비회원 인증 용도)
    @GetMapping("/id")
    public Long getIdByOrderNumber(@RequestParam String orderNumber) {
        return orderInternalService.findIdByOrderNumber(orderNumber);
    }

    // 주문ID로 주문번호 조회 (for 포인트 내역 테이블)
    @GetMapping("/{orderId}/orderNumber")
    public String getOrderNumberById(@PathVariable Long orderId) {
        return orderInternalService.findOrderNumberById(orderId);
    }

    // 책 구매 여부 조회 (for 리뷰 테이블)
    @GetMapping("/exists")
    public boolean validatePurchase(@RequestParam Long userNo, @RequestParam Long bookId) {
        return orderInternalService.validatePurchase(userNo, bookId);
    }
}
