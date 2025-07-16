package com.nhnacademy.bookstoreorderapi.payment.dto.Request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record OrderPointMinusProcessRequest (@NotNull Long orderNo,
                                            @Min(0) int point,
                                            @NotNull PointType pointType) {
}
