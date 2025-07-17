package com.nhnacademy.bookstoreorderapi.order.dto.internal;

import java.time.LocalDateTime;

public record ScheduledOrderCompletion(
        String orderNumber,
        Long createdBy,
        LocalDateTime completionTime
) {}