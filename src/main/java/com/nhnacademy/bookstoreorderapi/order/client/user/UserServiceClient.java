package com.nhnacademy.bookstoreorderapi.order.client.user;

import com.nhnacademy.bookstoreorderapi.order.client.user.dto.response.ResponsePointType;
import com.nhnacademy.bookstoreorderapi.order.client.user.dto.response.UserResponse;
import com.nhnacademy.bookstoreorderapi.order.client.user.dto.request.OrderPointMinusProcessRequest;
import com.nhnacademy.bookstoreorderapi.order.client.user.dto.request.OrderPointPlusProcessRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "USER-API")
public interface UserServiceClient {

    @GetMapping("/users/me")
    UserResponse getUserInfo(@RequestHeader("X-USER-ID") String userId);

    @PostMapping("/users/order-point/{userNo}/minus")
    void orderPointMinusProcess(@PathVariable("userNo") Long userNo,
                                @RequestBody OrderPointMinusProcessRequest request);

    @PostMapping("/users/order-point/{userNo}/plus")
    void orderPointPlusProcess(@PathVariable("userNo") Long userNo,
                               @RequestBody OrderPointPlusProcessRequest request);

    @GetMapping("/users/{userNo}/earning-rate")
    ResponsePointType getEarningRateByUserNo(@PathVariable("userNo") Long userNo);

    @GetMapping("/users/me/my-point")
    Integer getUserPoint(@RequestHeader("X-USER-ID") String userId);

    @GetMapping("/users/{userNo}/my-point")
    Integer getUserPointByUserNo(@PathVariable("userNo") Long userNo);
}
