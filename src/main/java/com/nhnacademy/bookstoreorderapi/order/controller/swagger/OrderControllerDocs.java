package com.nhnacademy.bookstoreorderapi.order.controller.swagger;

import com.nhnacademy.bookstoreorderapi.common.dto.ErrorResponse;
import com.nhnacademy.bookstoreorderapi.order.dto.request.CreateOrderRequest;
import com.nhnacademy.bookstoreorderapi.order.dto.request.OrderStatusRequest;
import com.nhnacademy.bookstoreorderapi.order.dto.request.UpdateOrderRequest;
import com.nhnacademy.bookstoreorderapi.order.dto.response.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@Tag(name = "주문 API", description = "주문 관련 API")
public interface OrderControllerDocs {

    @Operation(
            summary = "주문 생성",
            description = "회원 또는 비회원 주문을 생성합니다."
    )
    @ApiResponse(
            responseCode = "201",
            description = "주문 생성 성공",
            content = @Content(schema = @Schema(implementation = CreateOrderResponse.class))
    )
    @ApiResponse(
            responseCode = "400",
            description = "잘못된 요청",
            content = @Content(
                    schema = @Schema(implementation = ErrorResponse.class),
                    examples = @ExampleObject(
                            name = "잘못된 요청",
                            description = "요청 객체 값 검증 실패",
                            value = "{\"errorCode\": \"BAD_REQUEST\", \"errorMessage\": \"(값 검증에 실패한 필드에 대한 메시지)\"}"
                    )
            )
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
    ResponseEntity<CreateOrderResponse> createOrder(
            @Parameter(description = "주문 생성 요청 정보", required = true)
            @Valid @RequestBody CreateOrderRequest request,
            
            @Parameter(description = "사용자 ID (선택사항, 비회원의 경우 null)")
            @RequestHeader(value = "X-USER-ID", required = false) String xUserId
    );

    @Operation(
            summary = "미완료 주문 조회",
            description = "주문서 작성 페이지에서 사용하는 미완료 주문 정보를 조회합니다."
    )
    @ApiResponse(
            responseCode = "200",
            description = "조회 성공",
            content = @Content(schema = @Schema(implementation = CreateOrderResponse.class))
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
    ResponseEntity<CreateOrderResponse> getUnfinishedOrder(
            @Parameter(description = "주문 번호", required = true)
            @PathVariable String orderNumber,
            
            @Parameter(description = "사용자 ID (선택사항)")
            @RequestHeader(value = "X-USER-ID", required = false) String xUserId
    );

    @Operation(
            summary = "회원 주문 전체 조회",
            description = "특정 회원의 모든 주문을 페이징하여 조회합니다."
    )
    @ApiResponse(
            responseCode = "200",
            description = "조회 성공",
            content = @Content(schema = @Schema(implementation = Page.class))
    )
    @ApiResponse(
            responseCode = "401",
            description = "인증 실패",
            content = @Content(
                    schema = @Schema(implementation = ErrorResponse.class),
                    examples = @ExampleObject(
                            value = "{\"errorCode\": \"UNAUTHORIZED\", \"errorMessage\": \"주문 전체 조회 기능은 회원에게만 제공합니다\"}"
                    )
            )
    )
    ResponseEntity<Page<OrderSummaryResponse>> getAllOrdersByUserId(
            @Parameter(description = "사용자 ID", required = true)
            @RequestHeader("X-USER-ID") String xUserId,
            
            @Parameter(description = "페이징 정보")
            Pageable pageable
    );

    @Operation(
            summary = "주문 상세 조회",
            description = "주문 번호로 주문 상세 정보를 조회합니다."
    )
    @ApiResponse(
            responseCode = "200",
            description = "조회 성공",
            content = @Content(schema = @Schema(implementation = OrderDetailResponse.class))
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
    @ApiResponse(
            responseCode = "500",
            description = "내부 서비스 호출 오류",
            content = @Content(
                    schema = @Schema(implementation = ErrorResponse.class),
                    examples = @ExampleObject(
                            value = "{\"errorCode\": \"INTERNAL_SERVER_ERROR\", \"errorMessage\": \"내부 서비스 호출 오류 - (내부 서비스 이름): 오류 메시지\"}"
                    )
            )
    )
    ResponseEntity<OrderDetailResponse> getOrder(
            @Parameter(description = "주문 번호", required = true)
            @PathVariable String orderNumber,
            
            @Parameter(description = "사용자 ID (선택사항)")
            @RequestHeader(value = "X-USER-ID", required = false) String xUserId
    );

    @Operation(
            summary = "주문 업데이트",
            description = "포장 및 배송 정보를 업데이트합니다."
    )
    @ApiResponse(
            responseCode = "200",
            description = "업데이트 성공",
            content = @Content(schema = @Schema(implementation = OrderResponse.class))
    )
    @ApiResponse(
            responseCode = "400",
            description = "잘못된 요청 (주문번호가 비어있을 때)",
            content = @Content(
                    schema = @Schema(implementation = ErrorResponse.class),
                    examples = @ExampleObject(
                            value = "{\"errorCode\": \"BAD_REQUEST\", \"errorMessage\": \"주문번호가 비어있습니다.\"}"
                    )
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "주문 또는 포장지를 찾을 수 없음",
            content = @Content(
                    schema = @Schema(implementation = ErrorResponse.class),
                    examples = {
                            @ExampleObject(
                                    name = "주문 없음",
                                    description = "주문을 찾을 수 없는 경우",
                                    value = "{\"errorCode\": \"NOT_FOUND\", \"errorMessage\": \"주문을 찾을 수 없습니다: orderNumber=202507-abcdef-123456\"}"
                            ),
                            @ExampleObject(
                                    name = "포장지 없음",
                                    description = "포장지를 찾을 수 없는 경우",
                                    value = "{\"errorCode\": \"NOT_FOUND\", \"errorMessage\": \"포장지를 찾을 수 없습니다: wrappingId=1\"}"
                            )
                    }
            )
    )
    ResponseEntity<OrderResponse> updateOrder(
            @Parameter(description = "주문 번호", required = true)
            @PathVariable String orderNumber,
            
            @Parameter(description = "주문 업데이트 정보", required = true)
            @Valid @RequestBody UpdateOrderRequest request,
            
            @Parameter(description = "사용자 ID (선택사항)")
            @RequestHeader(value = "X-USER-ID", required = false) String xUserId
    );

    @Operation(
            summary = "주문 상태 변경 (취소/반품)",
            description = """
                    주문 상태를 변경합니다. action 필드로 구분됩니다:
                    
                    **CANCEL**: 주문 취소 - 결제 취소가 함께 처리됩니다
                    **RETURN**: 주문 반품 - 상품 수령 후 반품 처리됩니다
                    
                    각 action에 따라 다른 응답이 반환됩니다.
                    """
    )
    @ApiResponse(
            responseCode = "200",
            description = "상태 변경 성공",
            content = @Content(
                    schema = @Schema(implementation = OrderStatusResult.class),
                    examples = {
                            @ExampleObject(
                                    name = "취소 성공",
                                    description = "주문 취소 시 결제 정보가 포함된 응답",
                                    value = """
                                            {
                                              "type": "cancel",
                                              "payment": {
                                                "paymentId": 1,
                                                "orderId": "202507-abcdef-123456",
                                                "payType": "CARD",
                                                "payAmount": 50000,
                                                "paymentStatus": "CANCEL"
                                              }
                                            }
                                            """
                            ),
                            @ExampleObject(
                                    name = "반품 성공",
                                    description = "주문 반품 시 주문 정보가 포함된 응답",
                                    value = """
                                            {
                                              "type": "return",
                                              "order": {
                                                "orderId": 1,
                                                "orderNumber": "202507-abcdef-123456",
                                                "status": "RETURNED",
                                                "totalPrice": 50000,
                                                "receiverName": "홍길동"
                                              }
                                            }
                                            """
                            )
                    }
            )
    )
    @ApiResponse(
            responseCode = "400",
            description = "잘못된 요청 (유효하지 않은 주문 상태 변경)",
            content = @Content(
                    schema = @Schema(implementation = ErrorResponse.class),
                    examples = {
                            @ExampleObject(
                                name = "취소할 수 없음",
                                description = "배송 대기 상태만 취소가 가능함",
                                value = "{\"errorCode\": \"BAD_REQUEST\", \"errorMessage\": \"취소할 수 없는 주문 상태입니다: (주문상태)\"}"
                            ),
                            @ExampleObject(
                                    name = "반품기간이 지남",
                                    description = "파손/파본에 의한 반품은 출고일 기준 30일 이내, 그외에는 출고일 기준 10일 이내 반품 가능함",
                                    value = "{\"errorCode\": \"BAD_REQUEST\", \"errorMessage\": \"반품 가능한 기간이 지났습니다.\"}"
                            ),
                            @ExampleObject(
                                    name = "반품할 수 없음",
                                    description = "주문 상태가 '배송 완료'인 상품만 반품 가능함",
                                    value = "{\"errorCode\": \"BAD_REQUEST\", \"errorMessage\": \"반품 가능한 주문이 아닙니다.\"}"
                            )
                    }
            )
    )
    @ApiResponse(
            responseCode = "401",
            description = "인증 실패 (비회원은 취소, 반품 불가능)",
            content = @Content(
                    schema = @Schema(implementation = ErrorResponse.class),
                    examples = {
                            @ExampleObject(
                                    name = "회원이 아님",
                                    description = "비회원이 주문 상태 변경을 요청하는 경우",
                                    value = "{\"errorCode\": \"UNAUTHORIZED\", \"errorMessage\": \"회원만 주문 상태 변경이 가능합니다.\"}"
                            ),
                            @ExampleObject(
                                    name = "본인이 아님",
                                    description = "회원이지만 본인 주문이 아닌데 상태 변경 요청이 들어오는 경우",
                                    value = "{\"errorCode\": \"UNAUTHORIZED\", \"errorMessage\": \"본인의 주문만 취소, 반품할 수 있습니다.\"}"
                            ),

                    }
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
    ResponseEntity<OrderStatusResult> changeOrderStatus(
            @Parameter(description = "주문 번호", required = true)
            @PathVariable String orderNumber,
            
            @Parameter(description = "상태 변경 요청 정보", required = true)
            @Valid @RequestBody OrderStatusRequest request,
            
            @Parameter(description = "사용자 ID", required = true)
            @RequestHeader("X-USER-ID") String xUserId
    );
}