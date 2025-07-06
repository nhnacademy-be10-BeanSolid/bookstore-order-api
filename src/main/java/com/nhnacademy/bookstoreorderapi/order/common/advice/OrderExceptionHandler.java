package com.nhnacademy.bookstoreorderapi.order.common.advice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class OrderExceptionHandler {

    /**
     * Bean Validation 검증 실패 예외를 처리합니다.
     *
     * <p>입력값 검증 실패 시 다음과 같은 형태로 응답합니다:
     * <pre>
     * {
     *     "status": 400,
     *     "error": "Bean Validation Failed",
     *     "fieldErrors": {
     *         "fieldName": "error Message"
     *     }
     * }
     * </pre>
     *
     * @param ex MethodArgumentNotValidException 객체
     * @return ResponseEntity 타입의 응답
     *
     * @author mni-js
     * @since 2025-07-06
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("status", HttpStatus.BAD_REQUEST.value());
        errorResponse.put("error", "Bean Validation Failed");
        
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                fieldErrors.put(error.getField(), error.getDefaultMessage()));
        errorResponse.put("fieldErrors", fieldErrors);
        
        return ResponseEntity.badRequest().body(errorResponse);
    }
}
