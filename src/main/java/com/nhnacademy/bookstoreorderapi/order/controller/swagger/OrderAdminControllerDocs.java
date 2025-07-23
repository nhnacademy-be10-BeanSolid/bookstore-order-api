package com.nhnacademy.bookstoreorderapi.order.controller.swagger;

import com.nhnacademy.bookstoreorderapi.common.dto.ErrorResponse;
import com.nhnacademy.bookstoreorderapi.order.dto.response.OrderResponse;
import com.nhnacademy.bookstoreorderapi.order.dto.response.OrderSummaryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@Tag(name = "관리자 주문 API", description = "관리자용 주문 관리 API")
public interface OrderAdminControllerDocs {

    @Operation(
            summary = "모든 주문 조회",
            description = "모든 회원의 모든 주문을 페이징하여 조회합니다. (관리자 전용)"
    )
    @ApiResponse(
            responseCode = "200",
            description = "조회 성공",
            content = @Content(schema = @Schema(implementation = Page.class))
    )
    @ApiResponse(
            responseCode = "403",
            description = "관리자 권한 필요",
            content = @Content(
                    schema = @Schema(implementation = ErrorResponse.class),
                    examples = @ExampleObject(
                            value = "{\"errorCode\": \"FORBIDDEN\", \"errorMessage\": \"관리자 권한이 필요합니다\"}"
                    )
            )
    )
    ResponseEntity<Page<OrderSummaryResponse>> getAllOrders(
            @Parameter(description = "관리자 사용자 ID", required = true)
            @RequestHeader("X-USER-ID") String xUserId,
            
            @Parameter(description = "페이징 정보")
            Pageable pageable
    );

    @Operation(
            summary = "주문 상태를 배송중으로 변경",
            description = "주문 상태를 'PENDING'에서 'SHIPPING'으로 변경합니다. (관리자 전용)"
    )
    @ApiResponse(
            responseCode = "200",
            description = "상태 변경 성공",
            content = @Content(schema = @Schema(implementation = OrderResponse.class))
    )
    @ApiResponse(
            responseCode = "403",
            description = "관리자 권한 필요",
            content = @Content(
                    schema = @Schema(implementation = ErrorResponse.class),
                    examples = @ExampleObject(
                            value = "{\"errorCode\": \"FORBIDDEN\", \"errorMessage\": \"관리자 권한이 필요합니다\"}"
                    )
            )
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
    ResponseEntity<OrderResponse> changeStatusToShipping(
            @Parameter(description = "주문 번호", required = true)
            @PathVariable String orderNumber,
            
            @Parameter(description = "관리자 X-USER-ID", required = true)
            @RequestHeader("X-USER-ID") String xUserId
    );
}