package com.nhnacademy.bookstoreorderapi.order.domain.exception;

public class WrappingNotFoundException extends RuntimeException {
    private final Long wrappingId;

    public WrappingNotFoundException(Long wrappingId) {
        super("유효하지 않은 포장 ID: " + wrappingId);
        this.wrappingId = wrappingId;
    }

    public Long getWrappingId() {
        return wrappingId;
    }
    // 포장 옵션을 조회하려고 할때 해당 ID의 포장 정보가 없을 때 던진다.
}