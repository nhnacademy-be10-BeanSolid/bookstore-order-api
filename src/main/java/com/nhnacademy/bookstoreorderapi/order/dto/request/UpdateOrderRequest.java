package com.nhnacademy.bookstoreorderapi.order.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.util.List;

@Schema(description = "주문 업데이트 요청 DTO")
public record UpdateOrderRequest(

        @NotEmpty(message = "구매할 상품을 추가해주세요(현재: 구매할 상품 없음)")
        @Valid
        @Schema(description = "포장 요청 목록", requiredMode = Schema.RequiredMode.REQUIRED)
        List<WrappingRequest> wrappingRequests,

        @NotBlank(message = "배송 받을 사람을 적어주세요")
        @Schema(description = "받는 사람 이름", example = "홍길동", requiredMode = Schema.RequiredMode.REQUIRED)
        String receiverName,

        @NotBlank(message = "배송 받을 사람 전화번호를 입력해주세요")
        @Pattern(regexp = "^01\\d-\\d{4}-\\d{4}$",
                message = "올바른 휴대폰 번호 형식이 아닙니다 (올바른 형식: 010-1234-5678)")
        @Schema(description = "받는 사람 전화번호", example = "010-1234-5678", requiredMode = Schema.RequiredMode.REQUIRED)
        String receiverPhoneNumber,

        @NotBlank(message = "배송지를 선택해주세요")
        @Schema(description = "배송지 주소", example = "서울시 강남구 테헤란로 123", requiredMode = Schema.RequiredMode.REQUIRED)
        String address,

        @Future(message = "배송 요청 날짜는 주문일 다음 날부터 가능합니다")
        @Schema(description = "배송 요청 날짜", example = "2024-12-25")
        LocalDate requestedDeliveryDate
) {
    @Schema(description = "포장 요청 정보")
    public record WrappingRequest(

            @NotNull @Positive
            @Schema(description = "도서 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
            Long bookId,

            @Positive
            @Schema(description = "포장 ID", example = "1")
            Long wrappingId
    ) {}
}
