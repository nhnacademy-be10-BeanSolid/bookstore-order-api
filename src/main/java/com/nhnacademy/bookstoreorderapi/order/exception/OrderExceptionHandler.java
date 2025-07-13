package com.nhnacademy.bookstoreorderapi.order.exception;

import com.nhnacademy.bookstoreorderapi.common.dto.ErrorResponse;
import com.nhnacademy.bookstoreorderapi.order.client.user.exception.NotAdminException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class OrderExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class) // RequestBody의 dto 필드에 검증 어노테이션이 있을 때
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return createBadRequestResponse(errorMessage);
    }

    @ExceptionHandler({
            InvalidRequestException.class,
            HttpMessageNotReadableException.class, // 직렬화 실패
            HttpRequestMethodNotSupportedException.class
    })
    public ResponseEntity<ErrorResponse> handleBadRequestExceptions(Exception ex) {
        return createBadRequestResponse(ex.getMessage());
    }

    @ExceptionHandler(NotAdminException.class)
    public ResponseEntity<ErrorResponse> handleNotAdminException(NotAdminException ex) {
        return createErrorResponse(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    private ResponseEntity<ErrorResponse> createErrorResponse(HttpStatus status, String message) {
        return ResponseEntity.status(status)
                .body(new ErrorResponse(status.name(), message));
    }

    private ResponseEntity<ErrorResponse> createBadRequestResponse(String message) {
        return createErrorResponse(HttpStatus.BAD_REQUEST, message);
    }
}
