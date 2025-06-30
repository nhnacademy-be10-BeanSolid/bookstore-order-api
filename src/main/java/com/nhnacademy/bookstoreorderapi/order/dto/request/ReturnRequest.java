package com.nhnacademy.bookstoreorderapi.order.dto.request;

import lombok.*;

import java.time.LocalDateTime;

@Builder
public record ReturnRequest(
    String reason,
    LocalDateTime requestedAt,
    Boolean damaged
) {}
