package com.nhnacademy.bookstoreorderapi.payment.exception;

import com.nhnacademy.bookstoreorderapi.order.exception.notfound.OrderNotFoundException;
import com.nhnacademy.bookstoreorderapi.payment.dto.Response.ErrorResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler globalExceptionHandler;

    @BeforeEach
    void setUp() {
        globalExceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    void testHandleOrderNotFound() {
        OrderNotFoundException exception = new OrderNotFoundException("주문을 찾을 수 없습니다.");
        
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleOrderNotFound(exception);
        
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(404, response.getBody().getStatus());
        assertEquals("ORDER_NOT_FOUND", response.getBody().getCode());
        assertEquals("주문을 찾을 수 없습니다.", response.getBody().getMessage());
    }

    @Test
    void testHandleAlreadyPaid() {
        AlreadyPaidException exception = new AlreadyPaidException("order123");
        
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleAlreadyPaid(exception);
        
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(409, response.getBody().getStatus());
        assertEquals("ALREADY_PAID", response.getBody().getCode());
        assertEquals("이미 결제 완료된 주문입니다.", response.getBody().getMessage());
    }

    @Test
    void testHandleRedirectMissing() {
        RedirectUrlNotFoundException exception = new RedirectUrlNotFoundException("리다이렉트 URL을 찾을 수 없습니다.");
        
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleRedirectMissing(exception);
        
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(500, response.getBody().getStatus());
        assertEquals("REDIRECT_URL_NOT_FOUND", response.getBody().getCode());
    }

    @Test
    void testHandlePaymentCreation() {
        PaymentCreationException exception = new PaymentCreationException("결제 생성에 실패했습니다.");
        
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handlePaymentCreation(exception);
        
        assertEquals(HttpStatus.BAD_GATEWAY, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(502, response.getBody().getStatus());
        assertEquals("PAYMENT_CREATION_FAILED", response.getBody().getCode());
        assertEquals("결제 생성에 실패했습니다.", response.getBody().getMessage());
    }

    @Test
    void testHandlePaymentConfirmation() {
        PaymentConfirmationException exception = new PaymentConfirmationException("결제 승인에 실패했습니다.");
        
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handlePaymentConfirmation(exception);
        
        assertEquals(HttpStatus.BAD_GATEWAY, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(502, response.getBody().getStatus());
        assertEquals("PAYMENT_CONFIRMATION_FAILED", response.getBody().getCode());
        assertEquals("결제 승인에 실패했습니다.", response.getBody().getMessage());
    }

    @Test
    void testHandlePaymentNotFound() {
        PaymentNotFoundException exception = new PaymentNotFoundException("결제 정보를 찾을 수 없습니다.");
        
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handlePaymentNotFount(exception);
        
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(404, response.getBody().getStatus());
        assertEquals("PAYMENT_NOT_FOUND", response.getBody().getCode());
    }

    @Test
    void testHandlePaymentInfoFetch() {
        PaymentInfoFetchException exception = new PaymentInfoFetchException("결제 정보 조회에 실패했습니다.");
        
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handlePaymentInfoFetch(exception);
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().getStatus());
        assertEquals("PAYMENT_INFO_FETCH_FAILED", response.getBody().getCode());
        assertEquals("결제 정보 조회에 실패했습니다.", response.getBody().getMessage());
    }

    @Test
    void testHandleAll() {
        Exception exception = new Exception("예상치 못한 오류");
        
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleAll(exception);
        
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(500, response.getBody().getStatus());
        assertEquals("INTERNAL_SERVER_ERROR", response.getBody().getCode());
        assertEquals("서버 오류가 발생했습니다.", response.getBody().getMessage());
    }

    @Test
    void testHandleAllWithRuntimeException() {
        RuntimeException exception = new RuntimeException("런타임 오류");
        
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleAll(exception);
        
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(500, response.getBody().getStatus());
        assertEquals("INTERNAL_SERVER_ERROR", response.getBody().getCode());
        assertEquals("서버 오류가 발생했습니다.", response.getBody().getMessage());
    }

    @Test
    void testHandleAllWithNullPointerException() {
        NullPointerException exception = new NullPointerException("널 포인터 예외");
        
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleAll(exception);
        
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(500, response.getBody().getStatus());
        assertEquals("INTERNAL_SERVER_ERROR", response.getBody().getCode());
        assertEquals("서버 오류가 발생했습니다.", response.getBody().getMessage());
    }

    @Test
    void testErrorResponseBuilder() {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(400)
                .code("TEST_CODE")
                .message("테스트 메시지")
                .build();
        
        assertEquals(400, errorResponse.getStatus());
        assertEquals("TEST_CODE", errorResponse.getCode());
        assertEquals("테스트 메시지", errorResponse.getMessage());
    }

    @Test
    void testResponseEntityStatusCode() {
        AlreadyPaidException exception = new AlreadyPaidException("order123");
        
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleAlreadyPaid(exception);
        
        assertTrue(response.getStatusCode().is4xxClientError());
        assertEquals(409, response.getStatusCodeValue());
    }

    @Test
    void testResponseEntityBody() {
        PaymentCreationException exception = new PaymentCreationException("결제 생성 실패");
        
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handlePaymentCreation(exception);
        
        assertNotNull(response.getBody());
        assertTrue(response.hasBody());
        assertEquals("결제 생성 실패", response.getBody().getMessage());
    }
}