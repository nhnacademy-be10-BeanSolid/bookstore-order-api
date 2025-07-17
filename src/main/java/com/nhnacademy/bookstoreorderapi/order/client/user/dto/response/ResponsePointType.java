package com.nhnacademy.bookstoreorderapi.order.client.user.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResponsePointType {

    private Long typeId;
    private String typeName;
    private int earningPoint;
    private int earningRate;
    private String gradeName;

    @JsonProperty("isActive")
    private boolean isActive;
}
