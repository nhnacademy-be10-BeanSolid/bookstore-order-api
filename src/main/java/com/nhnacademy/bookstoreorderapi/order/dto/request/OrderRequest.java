package com.nhnacademy.bookstoreorderapi.order.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.time.LocalDate;
import java.util.List;

public record OrderRequest(
    @NotBlank
    String receiverName,

    @NotBlank(message = "배송지는 빈 문자열 혹은 공백이면 안됩니다")
    String address,

    @NotBlank
    String receiverPhoneNumber,

    @Future(message = "배송 요청일은 내일 이후여야 합니다.")
    LocalDate requestedDeliveryDate,

    @NotEmpty(message = "items 항목이 비어 있을 수 없습니다.")
    @Valid // @Valid가 있어야 OrderItemDto에 대한 값 검증이 이루어짐.(컬렉션 타입에 대한 재귀적 검증)
    List<OrderItemRequest> items
) {}
