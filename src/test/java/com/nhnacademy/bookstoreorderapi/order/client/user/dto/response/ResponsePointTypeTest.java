package com.nhnacademy.bookstoreorderapi.order.client.user.dto.response;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ResponsePointTypeTest {

    @Test
    void testNoArgsConstructor() {
        ResponsePointType responsePointType = new ResponsePointType();
        
        assertNotNull(responsePointType);
        assertNull(responsePointType.getTypeId());
        assertNull(responsePointType.getTypeName());
        assertEquals(0, responsePointType.getEarningPoint());
        assertEquals(0, responsePointType.getEarningRate());
        assertNull(responsePointType.getGradeName());
        assertFalse(responsePointType.isActive());
    }

    @Test
    void testAllArgsConstructor() {
        ResponsePointType responsePointType = new ResponsePointType(
                1L, "적립", 100, 5, "VIP", true
        );

        assertEquals(1L, responsePointType.getTypeId());
        assertEquals("적립", responsePointType.getTypeName());
        assertEquals(100, responsePointType.getEarningPoint());
        assertEquals(5, responsePointType.getEarningRate());
        assertEquals("VIP", responsePointType.getGradeName());
        assertTrue(responsePointType.isActive());
    }

    @Test
    void testSetters() {
        ResponsePointType responsePointType = new ResponsePointType();
        
        responsePointType.setTypeId(2L);
        responsePointType.setTypeName("차감");
        responsePointType.setEarningPoint(200);
        responsePointType.setEarningRate(10);
        responsePointType.setGradeName("GOLD");
        responsePointType.setActive(false);

        assertEquals(2L, responsePointType.getTypeId());
        assertEquals("차감", responsePointType.getTypeName());
        assertEquals(200, responsePointType.getEarningPoint());
        assertEquals(10, responsePointType.getEarningRate());
        assertEquals("GOLD", responsePointType.getGradeName());
        assertFalse(responsePointType.isActive());
    }

    @Test
    void testGetters() {
        ResponsePointType responsePointType = new ResponsePointType(
                3L, "환불", 300, 15, "SILVER", true
        );

        assertEquals(3L, responsePointType.getTypeId());
        assertEquals("환불", responsePointType.getTypeName());
        assertEquals(300, responsePointType.getEarningPoint());
        assertEquals(15, responsePointType.getEarningRate());
        assertEquals("SILVER", responsePointType.getGradeName());
        assertTrue(responsePointType.isActive());
    }

    @Test
    void testEqualsAndHashCode() {
        ResponsePointType type1 = new ResponsePointType(
                1L, "적립", 100, 5, "VIP", true
        );
        ResponsePointType type2 = new ResponsePointType(
                1L, "적립", 100, 5, "VIP", true
        );
        ResponsePointType type3 = new ResponsePointType(
                2L, "차감", 200, 10, "GOLD", false
        );

        assertEquals(type1, type2);
        assertNotEquals(type1, type3);
        assertEquals(type1.hashCode(), type2.hashCode());
        assertNotEquals(type1.hashCode(), type3.hashCode());
    }

    @Test
    void testEqualsWithNull() {
        ResponsePointType responsePointType = new ResponsePointType(
                1L, "적립", 100, 5, "VIP", true
        );

        assertNotEquals(null, responsePointType);
    }

    @Test
    void testEqualsWithDifferentClass() {
        ResponsePointType responsePointType = new ResponsePointType(
                1L, "적립", 100, 5, "VIP", true
        );
        String differentObject = "not a response point type";

        assertNotEquals(responsePointType, differentObject);
    }

    @Test
    void testToString() {
        ResponsePointType responsePointType = new ResponsePointType(
                1L, "적립", 100, 5, "VIP", true
        );

        String toString = responsePointType.toString();

        assertNotNull(toString);
        assertTrue(toString.contains("1"));
        assertTrue(toString.contains("적립"));
        assertTrue(toString.contains("100"));
        assertTrue(toString.contains("5"));
        assertTrue(toString.contains("VIP"));
        assertTrue(toString.contains("true"));
    }

    @Test
    void testJsonPropertyIsActive() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        
        // JSON -> Object 변환 테스트
        String json = "{\"typeId\":1,\"typeName\":\"적립\",\"earningPoint\":100,\"earningRate\":5,\"gradeName\":\"VIP\",\"isActive\":true}";
        ResponsePointType responsePointType = objectMapper.readValue(json, ResponsePointType.class);
        
        assertTrue(responsePointType.isActive());
        
        // Object -> JSON 변환 테스트
        ResponsePointType testObject = new ResponsePointType(1L, "적립", 100, 5, "VIP", true);
        String serializedJson = objectMapper.writeValueAsString(testObject);
        
        assertTrue(serializedJson.contains("\"isActive\":true"));
    }

    @Test
    void testAllFieldsWithNullValues() {
        ResponsePointType responsePointType = new ResponsePointType(
                null, null, 0, 0, null, false
        );

        assertNull(responsePointType.getTypeId());
        assertNull(responsePointType.getTypeName());
        assertEquals(0, responsePointType.getEarningPoint());
        assertEquals(0, responsePointType.getEarningRate());
        assertNull(responsePointType.getGradeName());
        assertFalse(responsePointType.isActive());
    }

    @Test
    void testNegativeValues() {
        ResponsePointType responsePointType = new ResponsePointType();
        responsePointType.setEarningPoint(-100);
        responsePointType.setEarningRate(-5);

        assertEquals(-100, responsePointType.getEarningPoint());
        assertEquals(-5, responsePointType.getEarningRate());
    }

    @Test
    void testBooleanActiveField() {
        ResponsePointType responsePointType = new ResponsePointType();
        
        // Default false
        assertFalse(responsePointType.isActive());
        
        // Set to true
        responsePointType.setActive(true);
        assertTrue(responsePointType.isActive());
        
        // Set back to false
        responsePointType.setActive(false);
        assertFalse(responsePointType.isActive());
    }

    @Test
    void testEqualsWithSameInstance() {
        ResponsePointType responsePointType = new ResponsePointType(
                1L, "적립", 100, 5, "VIP", true
        );

        assertEquals(responsePointType, responsePointType);
        assertEquals(responsePointType.hashCode(), responsePointType.hashCode());
    }
}