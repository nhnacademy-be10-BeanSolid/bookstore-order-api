package com.nhnacademy.bookstoreorderapi.order.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.nhnacademy.bookstoreorderapi.order.domain.entity.OrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class StatusChangeDto {

    @NotNull
    private final OrderStatus newStatus;

    @JsonCreator
    public StatusChangeDto(@JsonProperty("newStatus") OrderStatus newStatus) {
        this.newStatus = newStatus;
    }
}
