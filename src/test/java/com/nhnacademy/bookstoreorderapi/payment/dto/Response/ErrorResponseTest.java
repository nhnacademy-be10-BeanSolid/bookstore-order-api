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

    @Test
    void testEqualsWithNull() {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(400)
                .code("CODE1")
                .message("Message1")
                .build();
        
        assertNotEquals(null, errorResponse);
    }

    @Test
    void testEqualsWithDifferentClass() {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(400)
                .code("CODE1")
                .message("Message1")
                .build();
        String differentObject = "not an error response";
        
        assertNotEquals(errorResponse, differentObject);
    }

    @Test
    void testEqualsWithSameInstance() {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(400)
                .code("CODE1")
                .message("Message1")
                .build();
        
        assertEquals(errorResponse, errorResponse);
        assertEquals(errorResponse.hashCode(), errorResponse.hashCode());
    }

    @Test
    void testEqualsWithDifferentFields() {
        ErrorResponse response1 = ErrorResponse.builder()
                .status(400)
                .code("CODE1")
                .message("Message1")
                .build();
        
        ErrorResponse response2 = ErrorResponse.builder()
                .status(500)
                .code("CODE1")
                .message("Message1")
                .build();
        
        assertNotEquals(response1, response2);
        
        response2 = ErrorResponse.builder()
                .status(400)
                .code("CODE2")
                .message("Message1")
                .build();
        
        assertNotEquals(response1, response2);
        
        response2 = ErrorResponse.builder()
                .status(400)
                .code("CODE1")
                .message("Message2")
                .build();
        
        assertNotEquals(response1, response2);
    }

    @Test
    void testCanEqual() {
        ErrorResponse response1 = ErrorResponse.builder()
                .status(400)
                .code("CODE1")
                .message("Message1")
                .build();
        
        ErrorResponse response2 = ErrorResponse.builder()
                .status(500)
                .code("CODE2")
                .message("Message2")
                .build();
        
        Object differentType = new Object();
        
        assertTrue(response1.canEqual(response2));
        assertFalse(response1.canEqual(differentType));
        assertFalse(response1.canEqual(null));
    }
}
