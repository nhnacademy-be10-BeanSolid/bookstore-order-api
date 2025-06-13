package com.nhnacademy.bookstoreorderapi.order.dto;

import com.nhnacademy.bookstoreorderapi.order.domain.entity.OrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class StatusChangeRequestDto {
    @NotNull
    private OrderStatus newStatus;
    @NotNull
    private Long changedBy;
    private String memo;
}