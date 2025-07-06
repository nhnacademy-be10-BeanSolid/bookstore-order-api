package com.nhnacademy.bookstoreorderapi.order.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.util.List;

public record OrderRequest(

        @NotBlank(message = "받는 사람 이름을 입력해주세요")
        @Size(max = 20, message = "받는 사람 이름은 20자 이하로 입력해주세요")
        String receiverName,

        @NotBlank(message = "받는 사람 전화번호를 입력해주세요")
        @Pattern(regexp = "^01[0-9][0-9]{8}$",
                message = "올바른 휴대폰 번호 형식이 아닙니다 (올바른 형식: 01012345678)")
        String receiverPhoneNumber,

        @NotBlank(message = "우편번호를 선택해주세요")
        @Pattern(regexp = "^\\d{5}$", message = "우편번호는 5자리 숫자여야 합니다")
        String zipCode,

        @NotBlank(message = "주소를 선택해주세요")
        String baseAddress,

        @Size(max = 50, message = "상세 주소는 50자 이하로 입력해주세요")
        String detailAddress,

        @Future(message = "배송 요청 날짜는 주문일 다음 날부터 가능합니다")
        LocalDate requestedDeliveryDate,

        @NotEmpty(message = "주문 상품은 비어있을 수 없습니다")
        @Valid
        List<OrderItemRequest> orderItems
) {
    public record OrderItemRequest(

        @NotNull(message = "책 ID를 입력해주세요")
        @Positive(message = "책 ID는 양수여야 합니다")
        Long bookId,

        @NotNull(message = "수량을 입력해주세요")
        @Min(value = 1, message = "수량은 1개 이상이어야 합니다")
        Integer quantity,

        @NotNull(message = "가격을 입력해주세요")
        @Min(value = 0, message = "가격은 0원 이상이어야 합니다")
        Long price,

        @Positive(message = "포장 ID는 양수여야 합니다")
        Long wrappingId
    ) {}
}
