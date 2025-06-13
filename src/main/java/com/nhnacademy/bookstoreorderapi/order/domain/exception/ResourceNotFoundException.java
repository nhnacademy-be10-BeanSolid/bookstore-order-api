package com.nhnacademy.bookstoreorderapi.order.domain.exception;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);

        // 상태 로그, 취소 이력, 그외 다른 엔티티 조회에서 없다고 명시적으로 처리
    }
}