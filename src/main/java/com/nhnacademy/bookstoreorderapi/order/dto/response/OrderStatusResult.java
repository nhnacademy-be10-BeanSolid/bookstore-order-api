package com.nhnacademy.bookstoreorderapi.order.dto.response;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.nhnacademy.bookstoreorderapi.payment.dto.Response.PaymentResDto;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = OrderStatusResult.CancelResult.class, name = "cancel"),
    @JsonSubTypes.Type(value = OrderStatusResult.ReturnResult.class, name = "return")
})
public sealed interface OrderStatusResult 
    permits OrderStatusResult.CancelResult, OrderStatusResult.ReturnResult {
    
    record CancelResult(PaymentResDto payment) implements OrderStatusResult {}
    record ReturnResult(OrderResponse order) implements OrderStatusResult {}
}