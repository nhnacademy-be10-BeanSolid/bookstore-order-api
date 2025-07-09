package com.nhnacademy.bookstoreorderapi.order.common.advice;

import com.nhnacademy.bookstoreorderapi.common.dto.ErrorResponse;
import com.nhnacademy.bookstoreorderapi.order.client.user.exception.NotAdminException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class OrderExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .collect(Collectors.joining(", "));

        ErrorResponse resp = new ErrorResponse(HttpStatus.BAD_REQUEST.name(), errorMessage);
        
        return ResponseEntity.badRequest().body(resp);
    }

    @ExceptionHandler(NotAdminException.class)
    public ResponseEntity<ErrorResponse> handleNotAdminException(NotAdminException ex) {
        ErrorResponse errorMessage = new ErrorResponse(HttpStatus.FORBIDDEN.name(), ex.getMessage());

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorMessage);
    }
}
