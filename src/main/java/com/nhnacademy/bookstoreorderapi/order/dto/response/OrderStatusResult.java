package com.nhnacademy.bookstoreorderapi.order.dto.response;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.nhnacademy.bookstoreorderapi.payment.dto.Response.PaymentResDto;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = OrderStatusResult.CancelResult.class, name = "cancel"),
    @JsonSubTypes.Type(value = OrderStatusResult.ReturnResult.class, name = "return")
})
@Schema(description = "주문 상태 변경 결과", oneOf = {OrderStatusResult.CancelResult.class, OrderStatusResult.ReturnResult.class})
public sealed interface OrderStatusResult 
    permits OrderStatusResult.CancelResult, OrderStatusResult.ReturnResult {
    
    @Schema(description = "주문 취소 결과")
    record CancelResult(
            @Schema(description = "결제 정보")
            PaymentResDto payment
    ) implements OrderStatusResult {}
    
    @Schema(description = "주문 반품 결과")
    record ReturnResult(
            @Schema(description = "주문 정보")
            OrderResponse order
    ) implements OrderStatusResult {}
}