package com.nhnacademy.bookstoreorderapi.order.domain.entity;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

public enum OrderStatus {
    PENDING,  // 대기
    SHIPPING, // 배송중
    COMPLETED,// 완료
    RETURNED, // 반품
    CANCELED;  // 취소

    private static final Map<OrderStatus, Set<OrderStatus>> transitions = Map.of(
        PENDING, Set.of(SHIPPING, CANCELED),
        SHIPPING, Set.of(COMPLETED, CANCELED),
        COMPLETED, Set.of(RETURNED),
        RETURNED, Set.of(),
        CANCELED, Set.of()
    );

    public boolean canTransitionTo(OrderStatus next) {
        return transitions.getOrDefault(this, Collections.emptySet())
                .contains(next);
    }
}
