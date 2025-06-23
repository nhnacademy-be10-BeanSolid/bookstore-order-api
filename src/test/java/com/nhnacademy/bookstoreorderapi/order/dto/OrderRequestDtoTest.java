package com.nhnacademy.bookstoreorderapi.order.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class OrderRequestDtoTest {

    private static final String VALID_USER_ID = "user";
    private static final Long VALID_GUEST_ID = 1L;
    private static final String VALID_ADDRESS = "광주광역시";
    private static final LocalDate VALID_DATE = LocalDate.now().plusDays(1);
    private static final List<OrderItemRequestDto> VALID_ITEMS =
            List.of(new OrderItemRequestDto(1L, 1, 1L));

    private Validator validator;

    private OrderRequestDto createDto(String userId, Long guestId) {
        return new OrderRequestDto(userId, guestId, VALID_ADDRESS, VALID_DATE, VALID_ITEMS);
    }

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("userId, guestId 둘 중 하나만 존재하면 테스트는 성공한다")
    void isExactlyOnePresent_Success() {

        OrderRequestDto testDto1 = createDto(VALID_USER_ID, null);
        OrderRequestDto testDto2 = createDto(null, VALID_GUEST_ID);

        Set<ConstraintViolation<OrderRequestDto>> validation1 = validator.validate(testDto1);
        assertThat(validation1).isEmpty();

        Set<ConstraintViolation<OrderRequestDto>> validation2 = validator.validate(testDto2);
        assertThat(validation2).isEmpty();
    }

    @Test
    @DisplayName("userId, guestId가 둘 다 존재하거나, 둘 다 null이면 테스트는 실패한다")
    void BothPresentOrBothNull_Fail() {

        OrderRequestDto testDto1 = createDto(VALID_USER_ID, VALID_GUEST_ID);
        OrderRequestDto testDto2 = createDto(null, null);

        Set<ConstraintViolation<OrderRequestDto>> validation1 = validator.validate(testDto1);
        assertThat(validation1).hasSize(1);
        assertThat(validation1)
                .extracting(ConstraintViolation::getMessage)
                .containsExactly("userId 또는 guestId 중 하나만 존재해야 합니다.");

        Set<ConstraintViolation<OrderRequestDto>> validation2 = validator.validate(testDto2);
        assertThat(validation2).hasSize(1);
        assertThat(validation2)
                .extracting(ConstraintViolation::getMessage)
                .containsExactly("userId 또는 guestId 중 하나만 존재해야 합니다.");
    }

    @ParameterizedTest(name = "userId: {0} -> message: {1}")
    @CsvSource({
            "'', userId는 비어있거나 공백 뿐이면 안됩니다.",
            "' ', userId는 비어있거나 공백 뿐이면 안됩니다."
    })
    void userIdIsEmpty_Fail(String userId, String message) {

        Set<ConstraintViolation<OrderRequestDto>> validation = validator.validate(createDto(userId, VALID_GUEST_ID));
        assertThat(validation).hasSize(1)
                .extracting(ConstraintViolation::getMessage)
                .containsExactly(message);
    }

    @ParameterizedTest(name = "guestId: {0} -> message: {1}")
    @CsvSource({
            "0, guestId는 양수여야 합니다.",
            "-1, guestId는 양수여야 합니다."
    })
    void guestIdIsZeroOrNegative_Fail(Long guestId, String message) {

        Set<ConstraintViolation<OrderRequestDto>> validation = validator.validate(createDto(null, guestId));
        assertThat(validation).hasSize(1)
                .extracting(ConstraintViolation::getMessage)
                .containsExactly(message);
    }

    @ParameterizedTest(name = "address: {0} -> message: {1}")
    @CsvSource({
            "'', 배송지는 빈 문자열 혹은 공백이면 안됩니다",
            "' ', 배송지는 빈 문자열 혹은 공백이면 안됩니다"
    })
    void addressIsBlank_Fail(String address, String message) {

        OrderRequestDto dto = createDto(VALID_USER_ID, null);
        dto.setAddress(address);

        Set<ConstraintViolation<OrderRequestDto>> validation = validator.validate(dto);
        assertThat(validation).hasSize(1)
                .extracting(ConstraintViolation::getMessage)
                .containsExactly(message);
    }

    @Test
    @DisplayName("배송 요청일이 내일 이전이면 테스트는 실패한다")
    void requestedDeliveryDateIsBeforeTomorrow_Fail() {
        OrderRequestDto dto = createDto(VALID_USER_ID, null);
        dto.setRequestedDeliveryDate(LocalDate.now());

        Set<ConstraintViolation<OrderRequestDto>> violation = validator.validate(dto);
        assertThat(violation).hasSize(1)
                .extracting(ConstraintViolation::getMessage)
                .containsExactly("배송 요청일은 내일 이후여야 합니다.");
    }

    @Test
    @DisplayName("items가 비어있으면 테스트는 실패한다")
    void itemsEmpty_Fail() {
        OrderRequestDto dto = createDto(VALID_USER_ID, null);
        dto.setItems(Collections.emptyList());

        Set<ConstraintViolation<OrderRequestDto>> validation = validator.validate(dto);
        assertThat(validation).hasSize(1)
                .extracting(ConstraintViolation::getMessage)
                .containsExactly( "items 항목이 비어 있을 수 없습니다.");
    }

    @Test
    @DisplayName("items 내부 요소가 유효하지 않으면 테스트는 실패한다")
    void itemsElementInvalid_Fail() {
        OrderItemRequestDto badItem = new OrderItemRequestDto(1L, 0, 1L);
        OrderRequestDto dto = createDto(VALID_USER_ID, null);
        dto.setItems(List.of(badItem));

        Set<ConstraintViolation<OrderRequestDto>> validation = validator.validate(dto);
        assertThat(validation).hasSize(1);
    }
}
