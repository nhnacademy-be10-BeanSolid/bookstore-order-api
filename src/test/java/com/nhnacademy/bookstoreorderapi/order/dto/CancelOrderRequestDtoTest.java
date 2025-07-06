//// src/test/java/com/nhnacademy/bookstoreorderapi/order/dto/CancelOrderRequestDtoTest.java
//package com.nhnacademy.bookstoreorderapi.order.dto;
//
//import org.junit.jupiter.api.Test;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//class CancelOrderRequestDtoTest {
//
//    @Test
//    void builderSetsReasonCorrectly() {
//        CancelOrderRequestDto dto = CancelOrderRequestDto.builder()
//            .reason("고객 변심")
//            .build();
//
//        assertThat(dto.getReason()).isEqualTo("고객 변심");
//    }
//
//    @Test
//    void setterAndGetterWork() {
//        CancelOrderRequestDto dto = new CancelOrderRequestDto();
//        dto.setReason("재고 없음");
//
//        assertThat(dto.getReason()).isEqualTo("재고 없음");
//    }
//}