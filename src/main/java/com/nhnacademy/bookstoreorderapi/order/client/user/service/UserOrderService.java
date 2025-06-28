package com.nhnacademy.bookstoreorderapi.order.client.user.service;

import com.nhnacademy.bookstoreorderapi.order.client.ExternalServiceException;
import com.nhnacademy.bookstoreorderapi.order.client.user.UserServiceClient;
import com.nhnacademy.bookstoreorderapi.order.client.user.dto.UserOrderResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserOrderService {

    private final UserServiceClient userServiceClient;

    @CircuitBreaker(name = "userService", fallbackMethod = "fallbackUserOrders")
    public UserOrderResponse getUserInfo(String userId) {
        return userServiceClient.getUserInfo(userId).getBody();
    }

    public UserOrderResponse fallbackUserOrders(String userId, Throwable t) {
        log.warn("Fallback - userId: {}", userId);
        throw new ExternalServiceException("UserServiceClient Error", t);
    }
}
