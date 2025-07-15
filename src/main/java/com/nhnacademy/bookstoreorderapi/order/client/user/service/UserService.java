package com.nhnacademy.bookstoreorderapi.order.client.user.service;

import com.nhnacademy.bookstoreorderapi.common.exception.ExternalServiceException;
import com.nhnacademy.bookstoreorderapi.order.exception.notfound.UserNotFoundException;
import com.nhnacademy.bookstoreorderapi.order.client.user.UserServiceClient;
import com.nhnacademy.bookstoreorderapi.order.client.user.dto.UserResponse;
import feign.FeignException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserServiceClient userServiceClient;

    @CircuitBreaker(name = "user-service", fallbackMethod = "fallbackGetUserInfo")
    public UserResponse getUserInfo(String userId) {
        log.debug("사용자 정보를 가져옵니다 - userId: {}", userId);
        return userServiceClient.getUserInfo(userId);
    }

    @CircuitBreaker(name = "user-service", fallbackMethod = "fallbackPlusPoint")
    public UserResponse plusPoint(Long userNo, int point) {
        log.debug("포인트 적립 api를 호출합니다: userNo={}, point={}", userNo, point);
        return userServiceClient.plusPoint(userNo, point);
    }

    public UserResponse fallbackGetUserInfo(String userId, Throwable t) {
        if (t instanceof FeignException.NotFound) {
            log.debug("유저를 찾을 수 없습니다 - userId: {}", userId);
            throw new UserNotFoundException("유저를 찾을 수 없습니다 - userId: " + userId);
        }

        log.warn("UserService getUserInfo fallback 실행 - userId: {}, error: {}", userId, t.getMessage());
        throw new ExternalServiceException("UserServiceClient 에러 발생", t);
    }

    public UserResponse fallbackPlusPoint(Long userNo, int point, Throwable t) {
        throw new ExternalServiceException("UserServiceClient 에러 발생", t);
    }
}
