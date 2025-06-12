package com.nhnacademy.bookstoreorderapi.order.dto;

import com.nhnacademy.bookstoreorderapi.order.domain.entity.OrderStatus;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

/**
 * 주문 상태 변경 요청용 DTO
 */
@Getter
public class StatusChangeDto {
    @NotNull
    private final OrderStatus newStatus;
    private final Long changedBy;
    private final String memo;

    @JsonCreator
    public StatusChangeDto(
            @JsonProperty("newStatus") OrderStatus newStatus,
            @JsonProperty("changedBy") Long changedBy,
            @JsonProperty("memo") String memo
    ) {
        this.newStatus = newStatus;
        this.changedBy = changedBy;
        this.memo = memo;
    }
}