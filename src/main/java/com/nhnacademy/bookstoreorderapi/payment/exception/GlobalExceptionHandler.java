package com.nhnacademy.bookstoreorderapi.payment.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.nhnacademy.bookstoreorderapi.payment.dto.Response.ErrorResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleOrderNotFound(OrderNotFoundException ex) {
        ErrorResponse body = ErrorResponse.builder()
                .status(HttpStatus.NOT_FOUND.value())
                .code("ORDER_NOT_FOUND")
                .message(ex.getMessage())
                .build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(AlreadyPaidException.class)
    public ResponseEntity<ErrorResponse> handleAlreadyPaid(AlreadyPaidException ex) {
        ErrorResponse body = ErrorResponse.builder()
                .status(HttpStatus.CONFLICT.value())
                .code("ALREADY_PAID")
                .message(ex.getMessage())
                .build();
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(RedirectUrlNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleRedirectMissing(RedirectUrlNotFoundException ex) {
        ErrorResponse body = ErrorResponse.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .code("REDIRECT_URL_NOT_FOUND")
                .message(ex.getMessage())
                .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    @ExceptionHandler(PaymentCreationException.class)
    public ResponseEntity<ErrorResponse> handlePaymentCreation(PaymentCreationException ex) {
        ErrorResponse body = ErrorResponse.builder()
                .status(HttpStatus.BAD_GATEWAY.value())
                .code("PAYMENT_CREATION_FAILED")
                .message(ex.getMessage())
                .build();
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(body);
    }
    @ExceptionHandler(PaymentConfirmationException.class)
    public ResponseEntity<ErrorResponse> handlePaymentConfirmation(PaymentConfirmationException ex) {
        ErrorResponse body = ErrorResponse.builder()
                .status(HttpStatus.BAD_GATEWAY.value())
                .code("PAYMENT_CONFIRMATION_FAILED")
                .message(ex.getMessage())
                .build();
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(body);
    }
    @ExceptionHandler(PaymentNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlePaymentNotFount(PaymentNotFoundException ex) {
        ErrorResponse body = ErrorResponse.builder()
                .status(HttpStatus.NOT_FOUND.value())
                .code("PAYMENT_NOT_FOUND")
                .message(ex.getMessage())
                .build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }
    @ExceptionHandler(PaymentInfoFetchException.class)
    public ResponseEntity<ErrorResponse> handlePaymentInfoFetch(PaymentInfoFetchException ex) {
        ErrorResponse body = ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .code("PAYMENT_INFO_FETCH_FAILED")
                .message(ex.getMessage())
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    // 필요에 따라 추가 예외 핸들러 삽입

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAll(Exception ex) {
        ErrorResponse body = ErrorResponse.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .code("INTERNAL_SERVER_ERROR")
                .message("서버 오류가 발생했습니다.")
                .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

}