package com.nhnacademy.bookstoreorderapi.order.controller.swagger;

import com.nhnacademy.bookstoreorderapi.common.dto.ErrorResponse;
import com.nhnacademy.bookstoreorderapi.order.dto.response.UserOrderAmountResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Tag(name = "내부 주문 API", description = "내부 서비스 간 통신용 주문 API")
public interface OrderInternalControllerDocs {

    @Operation(
            summary = "최근 3개월 사용자별 주문 금액 조회",
            description = "최근 3개월간 사용자별 순수 주문 금액을 그룹화하여 조회합니다. (내부 서비스용)"
    )
    @ApiResponse(
            responseCode = "200",
            description = "조회 성공",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = UserOrderAmountResponse.class)))
    )
    @ApiResponse(
            responseCode = "500",
            description = "서버 오류",
            content = @Content(
                    schema = @Schema(implementation = ErrorResponse.class),
                    examples = @ExampleObject(
                            value = "{\"errorCode\": \"INTERNAL_SERVER_ERROR\", \"errorMessage\": \"내부 서버 오류가 발생했습니다\"}"
                    )
            )
    )
    List<UserOrderAmountResponse> getOrderAmountGroupByUserLastThreeMonth();

    @Operation(
            summary = "주문번호로 주문 ID 조회",
            description = "주문번호로 주문 ID를 조회합니다. (비회원 인증 용도)"
    )
    @ApiResponse(
            responseCode = "200",
            description = "조회 성공",
            content = @Content(schema = @Schema(implementation = Long.class))
    )
    @ApiResponse(
            responseCode = "404",
            description = "주문을 찾을 수 없음",
            content = @Content(
                    schema = @Schema(implementation = ErrorResponse.class),
                    examples = @ExampleObject(
                            value = "{\"errorCode\": \"NOT_FOUND\", \"errorMessage\": \"주문을 찾을 수 없습니다: orderNumber=202507-abcdef-123456\"}"
                    )
            )
    )
    Long getIdByOrderNumber(
            @Parameter(description = "주문 번호", required = true)
            @RequestParam String orderNumber
    );

    @Operation(
            summary = "주문 ID로 주문번호 조회",
            description = "주문 ID로 주문번호를 조회합니다. (포인트 내역 테이블용)"
    )
    @ApiResponse(
            responseCode = "200",
            description = "조회 성공",
            content = @Content(schema = @Schema(implementation = String.class))
    )
    @ApiResponse(
            responseCode = "404",
            description = "주문을 찾을 수 없음",
            content = @Content(
                    schema = @Schema(implementation = ErrorResponse.class),
                    examples = @ExampleObject(
                            value = "{\"errorCode\": \"NOT_FOUND\", \"errorMessage\": \"주문을 찾을 수 없습니다: orderNumber=202507-abcdef-123456\"}"
                    )
            )
    )
    String getOrderNumberById(
            @Parameter(description = "주문 ID", required = true)
            @PathVariable Long orderId
    );

    @Operation(
            summary = "책 구매 여부 확인",
            description = "특정 사용자가 특정 책을 구매했는지 확인합니다. (리뷰 테이블용)"
    )
    @ApiResponse(
            responseCode = "200",
            description = "확인 완료",
            content = @Content(schema = @Schema(implementation = Boolean.class))
    )
    @ApiResponse(
            responseCode = "400",
            description = "잘못된 요청",
            content = @Content(
                    schema = @Schema(implementation = ErrorResponse.class),
                    examples = @ExampleObject(
                            value = "{\"errorCode\": \"BAD_REQUEST\", \"errorMessage\": \"유효하지 않은 요청입니다\"}"
                    )
            )
    )
    boolean validatePurchase(
            @Parameter(description = "사용자 번호", required = true)
            @RequestParam Long userNo,
            
            @Parameter(description = "책 ID", required = true)
            @RequestParam Long bookId
    );
}