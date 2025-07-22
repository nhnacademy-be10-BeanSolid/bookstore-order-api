package com.nhnacademy.bookstoreorderapi.payment.dto.Response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "결제 응답 DTO")
public class PaymentResDto {

    @Schema(description = "결제 ID (DB PK)", example = "1")
    private Long paymentId;        
    
    @Schema(description = "주문 ID", example = "202507-abcdef-123456")
    private String orderId;        
    
    @Schema(description = "결제 수단", example = "CARD")
    private String payType;        

    @Schema(description = "결제 금액", example = "15000")
    private Long payAmount;        
    
    @Schema(description = "주문 및 결제 제목", example = "도서 주문")
    private String payName;        
    
    @Schema(description = "결제 상태", example = "SUCCESS")
    private String paymentStatus;  
    
    @Schema(description = "토스 결제 키", example = "toss-payment-key-123")
    private String paymentKey;     

    @Schema(description = "성공 콜백 URL", example = "https://example.com/success")
    private String successUrl;     
    
    @Schema(description = "실패 콜백 URL", example = "https://example.com/fail")
    private String failUrl;
    
    @Schema(description = "리다이렉트 URL", example = "https://example.com/redirect")
    private String redirectUrl;    
}