package com.nhnacademy.bookstoreorderapi.order.controller;

import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleBadRequest(IllegalArgumentException ex) {
        log.warn("BadRequest: {}", ex.getMessage(), ex);
        return ResponseEntity
                .badRequest()
                .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(FeignException.NotFound.class)
    public ResponseEntity<Map<String, String>> handleFeignNotFound(FeignException.NotFound ex) {
        log.warn("Feign 404 Not Found: {}", ex.getMessage(), ex);
        return ResponseEntity
                .status(404)
                .body(Map.of("error", "외부 서비스에서 리소스를 찾을 수 없습니다."));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleAll(Exception ex) {
        // 스택트레이스를 로그에 찍어 줍니다.
        log.error("Unhandled exception occurred", ex);
        return ResponseEntity
                .status(500)
                .body(Map.of("error", "서버 내부 오류가 발생했습니다."));
    }
}