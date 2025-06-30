package com.nhnacademy.bookstoreorderapi.order.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.time.LocalDate;
import java.util.List;

public record OrderRequest(

    @NotBlank(message = "받는 사람 이름을 꼭 입력해주세요")
    String receiverName,

    @NotBlank(message = "배송지를 꼭 입력해주세요")
    String address,

    @NotBlank(message = "받는 사람 전화번호를 꼭 입력해주세요")
    String receiverPhoneNumber,

    @Future(message = "배송 요청일은 내일부터 지정 가능합니다")
    LocalDate requestedDeliveryDate,

    @NotEmpty(message = "주문할 상품이 없습니다")
    @Valid // @Valid가 있어야 OrderItemRequest에 대한 값 검증이 이루어짐.(컬렉션 타입에 대한 재귀적 검증)
    List<OrderItemRequest> items
) {}
