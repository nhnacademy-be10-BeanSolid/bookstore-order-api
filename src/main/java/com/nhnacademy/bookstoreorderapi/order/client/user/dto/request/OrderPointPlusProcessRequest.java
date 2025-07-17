package com.nhnacademy.bookstoreorderapi.order.client.user.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record OrderPointPlusProcessRequest(@NotNull Long orderNo,
                                       @Min(0) int point,
                                       @NotNull PointType pointType) {
}
