package com.nhnacademy.bookstoreorderapi.order.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
                .guestId(1L)
                .requestedDeliveryDate(LocalDate.now().plusDays(1))
                .items(items)
                .build();
    }

    @Test
    void whenDeliveryDateInPast_thenFutureOrPresentViolation() {
        OrderRequestDto dto = buildValidDto();
        dto.setRequestedDeliveryDate(LocalDate.now().minusDays(1));

        Set<ConstraintViolation<OrderRequestDto>> violations = validator.validate(dto);

        assertThat(violations).anyMatch(v ->
                v.getPropertyPath().toString().equals("requestedDeliveryDate")
                        && v.getMessage().contains("오늘 이후")      // ★ 변경
        );
    }
}