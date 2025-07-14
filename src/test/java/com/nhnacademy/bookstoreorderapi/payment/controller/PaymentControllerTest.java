//package com.nhnacademy.bookstoreorderapi.payment.controller;
//
//import com.nhnacademy.bookstoreorderapi.payment.domain.PayType;
//import com.nhnacademy.bookstoreorderapi.payment.dto.Request.CancelPaymentRequest;
//import com.nhnacademy.bookstoreorderapi.payment.dto.Request.PaymentReqDto;
//import com.nhnacademy.bookstoreorderapi.payment.dto.Response.PaymentResDto;
//import com.nhnacademy.bookstoreorderapi.payment.service.PaymentService;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.*;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.servlet.view.RedirectView;
//
//import java.util.Map;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.mockito.Mockito.*;
//
//class PaymentControllerTest {
//
//    @Mock
//    private PaymentService paymentService;
//
//    @InjectMocks
//    private PaymentController paymentController;
//
//    @BeforeEach
//    void init() {
//        MockitoAnnotations.openMocks(this);
//    }
//
//    @Test
//    void requestPayment_success() {
//        // given
//        PaymentReqDto dto = new PaymentReqDto();
//        dto.setPayAmount(1000L);
//        dto.setPayType(PayType.CARD);
//        dto.setPayName("카드결제");
//
//        PaymentResDto resDto = PaymentResDto.builder()
//                .paymentKey("pk-1")
//                .orderId("order-123")
//                .payAmount(1000L)
//                .payName("카드결제")
//                .payType("CARD")
//                .build();
//
//        when(paymentService.requestTossPayment("order-123", dto)).thenReturn(resDto);
//
//        // when
//        ResponseEntity<PaymentResDto> response = paymentController.requestPayment("order-123", dto);
//
//        // then
//        assertThat(response.getStatusCodeValue()).isEqualTo(201);
//        assertThat(response.getBody()).isNotNull();
//        assertThat(response.getBody().getPaymentKey()).isEqualTo("pk-1");
//    }
//
//    @Test
//    void getPaymentInfo_success() {
//        // given
//        PaymentResDto resDto = PaymentResDto.builder()
//                .paymentKey("pk-1")
//                .orderId("order-123")
//                .payAmount(1000L)
//                .build();
//
//        when(paymentService.getPaymentInfo("pk-1")).thenReturn(resDto);
//
//        // when
//        ResponseEntity<PaymentResDto> response = paymentController.getPaymentInfo("pk-1");
//
//        // then
//        assertThat(response.getStatusCodeValue()).isEqualTo(200);
//        assertThat(response.getBody().getOrderId()).isEqualTo("order-123");
//    }
//
//    @Test
//    void tossSuccess_validParams() {
//        // given
//        Map<String, String> params = Map.of(
//                "paymentKey", "pk-123",
//                "orderId", "order-1",
//                "amount", "1000"
//        );
//
//        // when
//        RedirectView redirectView = paymentController.tossSuccess(params);
//
//        // then
//        verify(paymentService).markSuccess("pk-123", "order-1", 1000L);
//    }
//
//    @Test
//    void tossFail_validParams() {
//        Map<String, String> params = Map.of(
//                "paymentKey", "pk-999",
//                "message", "카드 오류"
//        );
//
//        RedirectView redirectView = paymentController.tossFail(params);
//
//        verify(paymentService).markFail("pk-999", "카드 오류");
//    }
//
//    @Test
//    void refundCardPaymentLegacy_success() {
//        Map<String, Object> request = Map.of("amount", 1000, "reason", "테스트");
//        Map<String, Object> responseMap = Map.of("result", "ok");
//
//        when(paymentService.refundCardPayment("pk-legacy", request)).thenReturn(responseMap);
//
//        ResponseEntity<Map<String, Object>> response = paymentController.refundCardPaymentLegacy("pk-legacy", request);
//
//        assertThat(response.getStatusCodeValue()).isEqualTo(200);
//        assertThat(response.getBody()).containsEntry("result", "ok");
//    }
//
//    @Test
//    void cancelPayment_success() {
//        CancelPaymentRequest req = new CancelPaymentRequest("orderId", 2000L, "사용자 요청");
//        PaymentResDto dto = PaymentResDto.builder()
//                .paymentKey("pk-cancel")
//                .orderId("orderId")
//                .payAmount(2000L)
//                .build();
//
//        when(paymentService.refundCardPayment("pk-cancel", req)).thenReturn(dto);
//
//        ResponseEntity<PaymentResDto> response = paymentController.cancelPayment("pk-cancel", req);
//
//        assertThat(response.getStatusCodeValue()).isEqualTo(200);
//        assertThat(response.getBody().getOrderId()).isEqualTo("orderId");
//    }
//
//    @Test
//    void handleAll_exceptionTest() {
//        Exception ex = new RuntimeException("테스트 예외");
//
//        ResponseEntity<Map<String, Object>> response = paymentController.handleAll(ex);
//
//        assertThat(response.getStatusCodeValue()).isEqualTo(500);
//        assertThat(response.getBody()).containsKey("message");
//        assertThat(response.getBody().get("message")).isEqualTo("테스트 예외");
//    }
//}