package com.nhnacademy.bookstoreorderapi.order.common.dto;

import java.util.*;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorResponse {
    
    private int httpStatus;
    private String errorReason;
    private Map<String, String> fieldErrors = new HashMap<>();
}