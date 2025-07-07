package com.nhnacademy.bookstoreorderapi.order.common.dto;

import java.util.*;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorResponse {
    
    private final int httpStatus;
    private final String errorReason;
    private final Map<String, String> fieldErrors = new HashMap<>();
}