package com.nhnacademy.bookstoreorderapi.order.dto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class OrderRequestDtoTest {
    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    private OrderRequestDto buildValidDto() {
        List<OrderItemDto> items = new ArrayList<>();
        items.add(OrderItemDto.builder()
                .bookId(1L)
                .quantity(1)
                .giftWrapped(false)
                .build());

        return OrderRequestDto.builder()
                .orderType("guest")
                .guestName("홍길동")
                .guestPhone("010-1234-5678")
                .deliveryDate(LocalDate.now().plusDays(1))
                .items(items)
                .build();
    }

    @Test
    void whenDeliveryDateNull_thenNoConstraintViolation() {
        OrderRequestDto dto = buildValidDto();
        dto.setDeliveryDate(null);

        Set<ConstraintViolation<OrderRequestDto>> violations = validator.validate(dto);
        // deliveryDate가 null이어도 @NotNull 제거했으므로 위반 없음
        assertThat(violations).noneMatch(v -> v.getPropertyPath().toString().equals("deliveryDate"));
    }

    @Test
    void whenDeliveryDateInPast_thenFutureOrPresentViolation() {
        OrderRequestDto dto = buildValidDto();
        dto.setDeliveryDate(LocalDate.now().minusDays(1));

        Set<ConstraintViolation<OrderRequestDto>> violations = validator.validate(dto);
        assertThat(violations)
                .anyMatch(v ->
                        v.getPropertyPath().toString().equals("deliveryDate")
                                && v.getMessage().contains("오늘 또는 이후"));
    }
}