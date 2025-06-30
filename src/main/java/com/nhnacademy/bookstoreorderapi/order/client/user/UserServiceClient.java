package com.nhnacademy.bookstoreorderapi.order.client.user;

import com.nhnacademy.bookstoreorderapi.order.client.user.dto.UserOrderResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "USER-API")
public interface UserServiceClient {

    @GetMapping("/users/me")
    ResponseEntity<UserOrderResponse> getUserInfo(@RequestHeader("X-USER-ID") String userId);
}
