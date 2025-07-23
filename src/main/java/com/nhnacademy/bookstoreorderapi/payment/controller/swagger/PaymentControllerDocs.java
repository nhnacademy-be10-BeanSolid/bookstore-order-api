package com.nhnacademy.bookstoreorderapi.payment.controller.swagger;

import com.nhnacademy.bookstoreorderapi.payment.domain.PayType;
import com.nhnacademy.bookstoreorderapi.payment.dto.Request.PaymentApprovalRequestDto;
import com.nhnacademy.bookstoreorderapi.payment.dto.Request.PaymentReqDto;
import com.nhnacademy.bookstoreorderapi.payment.dto.Response.ErrorResponse;
import com.nhnacademy.bookstoreorderapi.payment.dto.Response.PaymentResDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.RedirectView;

import java.util.Map;

@Tag(name = "결제 API", description = "결제 관련 API")
public interface PaymentControllerDocs {

    @Operation(
            summary = "토스 결제 요청",
            description = "주문에 대한 토스 결제를 요청합니다."
    )
    @ApiResponse(
            responseCode = "201",
            description = "결제 요청 성공",
            content = @Content(schema = @Schema(implementation = PaymentResDto.class))
    )
    @ApiResponse(
            responseCode = "400",
            description = "잘못된 요청",
            content = @Content(
                    schema = @Schema(implementation = ErrorResponse.class),
                    examples = {
                            @ExampleObject(
                                    name = "필수 값 누락",
                                    description = "결제 요청 시 필수 값이 누락된 경우",
                                    value = "{\"status\": 400, \"code\": \"BAD_REQUEST\", \"message\": \"결제 금액(payAmount)는 필수입니다.\"}"
                            ),
                            @ExampleObject(
                                    name = "비회원 포인트 사용",
                                    description = "비회원이 포인트를 사용하려고 시도한 경우",
                                    value = "{\"status\": 400, \"code\": \"BAD_REQUEST\", \"message\": \"비회원은 포인트를 사용할 수 없습니다\"}"
                            ),
                            @ExampleObject(
                                    name = "포인트 부족",
                                    description = "사용하려는 포인트가 보유 포인트보다 많은 경우",
                                    value = "{\"status\": 400, \"code\": \"BAD_REQUEST\", \"message\": \"사용하려는 포인트가 부족합니다.\"}"
                            )
                    }
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "주문을 찾을 수 없음",
            content = @Content(
                    schema = @Schema(implementation = ErrorResponse.class),
                    examples = @ExampleObject(
                            value = "{\"status\": 404, \"code\": \"NOT_FOUND\", \"message\": \"주문을 찾을 수 없습니다: orderNumber=202507-abcdef-123456\"}"
                    )
            )
    )
    @ApiResponse(
            responseCode = "409",
            description = "이미 결제된 주문임",
            content = @Content(
                    schema = @Schema(implementation = ErrorResponse.class),
                    examples = @ExampleObject(
                            value = "{\"status\": 409, \"code\": \"ALREADY_PAID\", \"message\": \"이미 결제 완료된 주문입니다: orderNumber=202507-abcdef-123456\"}"
                    )
            )
    )
    @ApiResponse(
            responseCode = "502",
            description = "외부 서비스 오류",
            content = @Content(
                    schema = @Schema(implementation = ErrorResponse.class),
                    examples = {
                            @ExampleObject(
                                    name = "결제 생성 실패",
                                    description = "paymentKey가 없는 경우",
                                    value = "{\"status\": 502, \"code\": \"BAD_GATEWAY\", \"message\": \"paymentKey 없음\"}"
                            ),
                            @ExampleObject(
                                    name = "리다이렉트 URL 없음",
                                    description = "결제 리다이렉트 URL을 찾을 수 없는 경우",
                                    value = "{\"status\": 502, \"code\": \"INTERNAL_SERVER_ERROR\", \"message\": \"리다이렉트 URL 없음...\"}"
                            )
                    }
            )
    )
    @ApiResponse(
            responseCode = "500",
            description = "서버 오류",
            content = @Content(
                    schema = @Schema(implementation = ErrorResponse.class),
                    examples = @ExampleObject(
                            value = "{\"status\": 500, \"code\": \"INTERNAL_SERVER_ERROR\", \"message\": \"내부 서버 오류가 발생했습니다\"}"
                    )
            )
    )
    ResponseEntity<PaymentResDto> requestPayment(
            @Parameter(description = "주문 ID", required = true)
            @PathVariable String orderId,
            
            @Parameter(description = "결제 요청 정보", required = true)
            @RequestBody PaymentReqDto dto
    );

    @Operation(
            summary = "토스 결제 요청 (GET 방식)",
            description = "GET 요청을 통해 토스 결제를 요청합니다."
    )
    @ApiResponse(
            responseCode = "201",
            description = "결제 요청 성공",
            content = @Content(schema = @Schema(implementation = PaymentResDto.class))
    )
    @ApiResponse(
            responseCode = "400",
            description = "잘못된 요청",
            content = @Content(
                    schema = @Schema(implementation = ErrorResponse.class),
                    examples = {
                            @ExampleObject(
                                    name = "파라미터 누락",
                                    description = "필수 파라미터가 누락된 경우",
                                    value = "{\"status\": 400, \"code\": \"BAD_REQUEST\", \"message\": \"결제 금액(payAmount)는 필수입니다.\"}"
                            ),
                            @ExampleObject(
                                    name = "비회원 포인트 사용",
                                    description = "비회원이 포인트를 사용하려고 시도한 경우",
                                    value = "{\"status\": 400, \"code\": \"BAD_REQUEST\", \"message\": \"비회원은 포인트를 사용할 수 없습니다\"}"
                            )
                    }
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "주문을 찾을 수 없음",
            content = @Content(
                    schema = @Schema(implementation = ErrorResponse.class),
                    examples = @ExampleObject(
                            value = "{\"status\": 404, \"code\": \"NOT_FOUND\", \"message\": \"주문을 찾을 수 없습니다: orderId=202507-abcdef-123456\"}"
                    )
            )
    )
    @ApiResponse(
            responseCode = "409",
            description = "이미 결제된 주문임",
            content = @Content(
                    schema = @Schema(implementation = ErrorResponse.class),
                    examples = @ExampleObject(
                            value = "{\"status\": 409, \"code\": \"ALREADY_PAID\", \"message\": \"이미 결제 완료된 주문입니다: orderNumber=202507-abcdef-123456\"}"
                    )
            )
    )
    @ApiResponse(
            responseCode = "502",
            description = "외부 서비스 오류",
            content = @Content(
                    schema = @Schema(implementation = ErrorResponse.class),
                    examples = @ExampleObject(
                            value = "{\"status\": 502, \"code\": \"PAYMENT_CREATION_FAILED\", \"message\": \"결제 생성에 실패했습니다\"}"
                    )
            )
    )
    ResponseEntity<PaymentResDto> requestPaymentViaGet(
            @Parameter(description = "주문 ID", required = true)
            @PathVariable String orderId,
            
            @Parameter(description = "결제 수단", required = true)
            @RequestParam PayType payType,
            
            @Parameter(description = "결제명", required = true)
            @RequestParam String payName,
            
            @Parameter(description = "결제 금액", required = true)
            @RequestParam Long payAmount
    );

    // 이 메서드는 사용되는 곳이 없음. 삭제해도 될 것 같음.
    @Operation(
            summary = "결제 정보 조회",
            description = "결제 키로 결제 정보를 조회합니다."
    )
    @ApiResponse(
            responseCode = "200",
            description = "조회 성공",
            content = @Content(schema = @Schema(implementation = PaymentResDto.class))
    )
    @ApiResponse(
            responseCode = "401",
            description = "인증되지 않은 키",
            content = @Content(
                    schema = @Schema(implementation = ErrorResponse.class),
                    examples = @ExampleObject(
                            value = "{\"status\": 400, \"code\": \"UNAUTHORIZED_KEY\", \"message\": \"인증되지 않은 시크릿 키 혹은 클라이언트 키 입니다.\"}"
                    )
            )
    )
    ResponseEntity<PaymentResDto> getPaymentInfo(
            @Parameter(description = "결제 키", required = true)
            @PathVariable String paymentKey
    );

    @Operation(
            summary = "결제 성공 처리",
            description = "토스에서 결제 성공 시 호출되는 콜백 API입니다."
    )
    @ApiResponse(
            responseCode = "200",
            description = "결제 성공 처리 완료",
            content = @Content(schema = @Schema(implementation = PaymentApprovalRequestDto.class))
    )
    @ApiResponse(
            responseCode = "404",
            description = "결제 또는 주문 정보를 찾을 수 없음",
            content = @Content(
                    schema = @Schema(implementation = ErrorResponse.class),
                    examples = {
                            @ExampleObject(
                                    name = "결제 없음",
                                    description = "결제 정보를 찾을 수 없는 경우",
                                    value = "{\"status\": 404, \"code\": \"NOT_FOUND\", \"message\": \"결제 내역을 찾을 수 없습니다: paymentKey=toss-payment-key-123\"}"
                            ),
                            @ExampleObject(
                                    name = "주문 없음",
                                    description = "주문 정보를 찾을 수 없는 경우",
                                    value = "{\"status\": 404, \"code\": \"NOT_FOUND\", \"message\": \"주문을 찾을 수 없습니다: orderId=202507-abcdef-123456\"}"
                            )
                    }
            )
    )
    @ApiResponse(
            responseCode = "502",
            description = "결제 승인 실패",
            content = @Content(
                    schema = @Schema(implementation = ErrorResponse.class),
                    examples = {
                            @ExampleObject(
                                    name = "토스 승인 실패",
                                    description = "토스 결제 승인에 실패한 경우",
                                    value = "{\"status\": 502, \"code\": \"PAYMENT_CONFIRMATION_FAILED\", \"message\": \"Toss confirm 실패: ...\"}"
                            )
                    }
            )
    )
    ResponseEntity<PaymentApprovalRequestDto> tossSuccess(
            @Parameter(description = "결제 키", required = true)
            @RequestParam("paymentKey") String paymentKey,
            
            @Parameter(description = "주문 ID", required = true)
            @RequestParam("orderId") String orderId,
            
            @Parameter(description = "결제 금액", required = true)
            @RequestParam("amount") long amount
    );

    @Operation(
            summary = "결제 실패 처리",
            description = "토스에서 결제 실패 시 호출되는 콜백 API입니다."
    )
    @ApiResponse(
            responseCode = "302",
            description = "결제 실패 페이지로 리다이렉트"
    )
    @ApiResponse(
            responseCode = "404",
            description = "결제 정보를 찾을 수 없음",
            content = @Content(
                    schema = @Schema(implementation = ErrorResponse.class),
                    examples = @ExampleObject(
                            value = "{\"status\": 404, \"code\": \"NOT_FOUND\", \"message\": \"결제 내역을 찾을 수 없습니다: paymentKey=toss-payment-key-123\"}"
                    )
            )
    )
    RedirectView tossFail(
            @Parameter(description = "실패 정보", required = true)
            @RequestParam Map<String, String> p
    );
}