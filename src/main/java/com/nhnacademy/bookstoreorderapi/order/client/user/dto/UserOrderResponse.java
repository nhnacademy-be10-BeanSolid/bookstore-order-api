package com.nhnacademy.bookstoreorderapi.order.client.user.dto;

import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
public record UserOrderResponse(
    Long userNo,
    String userId,
    String userPassword,
    String userName,
    String userPhoneNumber,
    String userEmail,
    LocalDate userBirth,
    int userPoint,
    boolean isAuth,
    String userStatus,
    LocalDateTime lastLoginAt,
    String userGradeName
) {}