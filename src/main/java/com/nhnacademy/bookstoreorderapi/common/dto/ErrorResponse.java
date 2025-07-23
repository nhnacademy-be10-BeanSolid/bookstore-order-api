package com.nhnacademy.bookstoreorderapi.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "에러 응답 DTO")
public record ErrorResponse(
        @Schema(description = "에러 코드")
        String errorCode,
        
        @Schema(description = "에러 메시지")
        String errorMessage
) {}
