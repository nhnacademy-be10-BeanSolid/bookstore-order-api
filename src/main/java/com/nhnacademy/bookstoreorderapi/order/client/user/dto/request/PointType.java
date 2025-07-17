package com.nhnacademy.bookstoreorderapi.order.client.user.dto.request;

import lombok.Getter;

@Getter
public enum PointType {
    ORDER(27L),
    RETURN(32L),
    CANCEL(33L);

    private final Long id;

    PointType(Long id) {
        this.id = id;
    }

}
