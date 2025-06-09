package com.nhnacademy.bookstoreorderapi.order.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "delivery-service",
        url  = "${delivery.service.url}"
)
public interface DeliveryClient {

    @PostMapping("/fees")
    DeliveryFeeResponse calculateFee(@RequestBody DeliveryFeeRequest request);

    // 요청 payload
    public static class DeliveryFeeRequest {
        private Long userId;
        private int  orderAmount;

        public DeliveryFeeRequest() {}
        public DeliveryFeeRequest(Long userId, int orderAmount) {
            this.userId      = userId;
            this.orderAmount = orderAmount;
        }
        public Long getUserId()        { return userId; }
        public int  getOrderAmount()   { return orderAmount; }
        public void setUserId(Long u)  { this.userId = u; }
        public void setOrderAmount(int a) { this.orderAmount = a; }
    }

    // 응답 payload
    public static class DeliveryFeeResponse {
        private int deliveryFee;
        public int getDeliveryFee()           { return deliveryFee; }
        public void setDeliveryFee(int fee)   { this.deliveryFee = fee; }
    }
}