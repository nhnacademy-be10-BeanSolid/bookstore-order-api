package com.nhnacademy.bookstoreorderapi.payment.dto.Response;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ErrorResponseTest {

    @Test
    void testBuilderAndGetters() {
        int status = 400;
        String code = "ERROR_CODE_1";
        String message = "Error message 1";

        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(status)
                .code(code)
                .message(message)
                .build();

        assertEquals(status, errorResponse.getStatus());
        assertEquals(code, errorResponse.getCode());
        assertEquals(message, errorResponse.getMessage());
    }

    @Test
    void testEqualsAndHashCode() {
        ErrorResponse errorResponse1 = ErrorResponse.builder().status(400).code("CODE1").message("Message1").build();
        ErrorResponse errorResponse2 = ErrorResponse.builder().status(400).code("CODE1").message("Message1").build();
        ErrorResponse errorResponse3 = ErrorResponse.builder().status(500).code("CODE2").message("Message2").build();

        assertEquals(errorResponse1, errorResponse2);
        assertNotEquals(errorResponse1, errorResponse3);
        assertEquals(errorResponse1.hashCode(), errorResponse2.hashCode());
        assertNotEquals(errorResponse1.hashCode(), errorResponse3.hashCode());
    }

    @Test
    void testToString() {
        ErrorResponse errorResponse = ErrorResponse.builder().status(400).code("CODE1").message("Message1").build();
        String toString = errorResponse.toString();
        assertTrue(toString.contains("status=400"));
        assertTrue(toString.contains("code=CODE1"));
        assertTrue(toString.contains("message=Message1"));
    }
}
