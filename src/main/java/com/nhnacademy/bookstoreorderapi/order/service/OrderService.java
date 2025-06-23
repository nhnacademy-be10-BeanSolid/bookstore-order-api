// src/main/java/com/nhnacademy/bookstoreorderapi/order/service/OrderService.java
package com.nhnacademy.bookstoreorderapi.order.service;

import com.nhnacademy.bookstoreorderapi.order.domain.entity.OrderStatus;
import com.nhnacademy.bookstoreorderapi.order.dto.*;

import java.util.List;

public interface OrderService {
    /**
     * 주문 생성 후 DB PK (order.id)를 리턴합니다.
     */
    long createOrder(OrderRequestDto req);

    /**
     * userId(로그인 ID)로 주문 목록 조회
     */
    List<OrderResponseDto> listByUser(String userId);

    /**
     * PK(order.id)로 주문 취소
     */
    void cancelOrder(long orderId, String reason);

    /**
     * PK(order.id)로 상태 변경
     */
    StatusChangeResponseDto changeStatus(
            long orderId,
            OrderStatus newStatus,
            long changedBy,
            String memo);

    /**
     * PK(order.id)로 반품 요청, 반품 후 환불금액을 돌려줍니다.
     */
    int requestReturn(long orderId, ReturnRequestDto req);

    /**
     * PK(order.id)로 상태 변경 로그 조회
     */
    List<OrderStatusLogDto> getStatusLog(long orderId);
}