package com.nhnacademy.bookstoreorderapi.order.client.user;

import com.nhnacademy.bookstoreorderapi.order.client.user.dto.UserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "USER-API")
public interface UserServiceClient {

    @GetMapping("/users/me")
    UserResponse getUserInfo(@RequestHeader("X-USER-ID") String userId);

    @PutMapping("/{userNo}/plus-point")
    UserResponse plusPoint(@PathVariable Long userNo, @RequestParam int point);
}
