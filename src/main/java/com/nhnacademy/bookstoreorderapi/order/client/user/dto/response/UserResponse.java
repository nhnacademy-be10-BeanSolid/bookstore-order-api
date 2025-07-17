package com.nhnacademy.bookstoreorderapi.order.client.user.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record UserResponse(
    Long userNo,
    String userId,
    String userPassword,
    String userName,
    String userPhoneNumber,
    String userEmail,
    LocalDate userBirth,
    int userPoint,
    @JsonProperty("auth")
    boolean isAuth,
    String userStatus,
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime createdAt,
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime lastLoginAt,
    String userGradeName
) {}