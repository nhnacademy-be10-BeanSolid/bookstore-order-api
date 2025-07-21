package com.nhnacademy.bookstoreorderapi.payment.service.impl;

import com.nhnacademy.bookstoreorderapi.common.service.PointService;
import com.nhnacademy.bookstoreorderapi.order.domain.Order;
import com.nhnacademy.bookstoreorderapi.order.domain.OrderStatus;
import com.nhnacademy.bookstoreorderapi.order.exception.notfound.OrderNotFoundException;
import com.nhnacademy.bookstoreorderapi.order.repository.OrderRepository;
import com.nhnacademy.bookstoreorderapi.payment.client.TossPaymentClient;
import com.nhnacademy.bookstoreorderapi.payment.config.TossPaymentConfig;
import com.nhnacademy.bookstoreorderapi.payment.domain.PayType;
import com.nhnacademy.bookstoreorderapi.payment.domain.PaymentStatus;
import com.nhnacademy.bookstoreorderapi.payment.domain.entity.Payment;
import com.nhnacademy.bookstoreorderapi.payment.dto.Request.CancelPaymentRequest;
import com.nhnacademy.bookstoreorderapi.payment.dto.Request.PaymentApprovalRequestDto;
import com.nhnacademy.bookstoreorderapi.payment.dto.Request.PaymentReqDto;
import com.nhnacademy.bookstoreorderapi.payment.dto.Response.PaymentResDto;
import com.nhnacademy.bookstoreorderapi.payment.exception.AlreadyPaidException;
import com.nhnacademy.bookstoreorderapi.payment.exception.PaymentCreationException;
import com.nhnacademy.bookstoreorderapi.payment.exception.PaymentNotFoundException;
import com.nhnacademy.bookstoreorderapi.payment.repository.PaymentRepository;
import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PaymentServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private TossPaymentClient tossPaymentClient;

    @Mock
    private TossPaymentConfig tossPaymentConfig;

    @Mock
    private PointService pointService;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private Order order;
    private Payment payment;
    private PaymentReqDto paymentReqDto;

    @BeforeEach
    void setUp() {
        order = mock(Order.class);
        given(order.getOrderNumber()).willReturn("testOrderNumber");

        payment = new Payment();
        payment.setPaymentKey("testPaymentKey");
        payment.setOrder(order);
        payment.setPayAmount(15000L);
        payment.setPayType(PayType.CARD);
        payment.setPayName("Test Book");

        paymentReqDto = new PaymentReqDto();
        paymentReqDto.setPayType(PayType.CARD);
        paymentReqDto.setPayAmount(15000L);
        paymentReqDto.setPayName("Test Book");
    }

    @Test
    @DisplayName("Toss 결제 요청 - 성공")
    void requestTossPayment_Success() {
        // given
        given(orderRepository.findByOrderNumber(anyString())).willReturn(Optional.of(order));
        given(paymentRepository.findByOrder(any(Order.class))).willReturn(Optional.empty());
        given(tossPaymentConfig.getSuccessUrl()).willReturn("http://localhost/success");
        given(tossPaymentConfig.getFailUrl()).willReturn("http://localhost/fail");
        Map<String, Object> tossApiResponse = Map.of("paymentKey", "newPaymentKey", "nextRedirectPcUrl", "http://redirect.url");
        given(tossPaymentClient.createPayment(anyMap())).willReturn(tossApiResponse);

        // when
        PaymentResDto result = paymentService.requestTossPayment("testOrderNumber", paymentReqDto);

        // then
        assertThat(result.getPaymentKey()).isEqualTo("newPaymentKey");
        assertThat(result.getRedirectUrl()).isEqualTo("http://redirect.url");
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    @DisplayName("Toss 결제 요청 - 주문을 찾을 수 없음")
    void requestTossPayment_OrderNotFound_ThrowsException() {
        // given
        given(orderRepository.findByOrderNumber(anyString())).willReturn(Optional.empty());

        // when & then
        assertThrows(OrderNotFoundException.class, () -> {
            paymentService.requestTossPayment("nonExistentOrder", paymentReqDto);
        });
    }

    @Test
    @DisplayName("Toss 결제 요청 - 이미 결제된 주문")
    void requestTossPayment_AlreadyPaid_ThrowsException() {
        // given
        payment.setPaymentStatus(PaymentStatus.SUCCESS);
        given(orderRepository.findByOrderNumber(anyString())).willReturn(Optional.of(order));
        given(paymentRepository.findByOrder(any(Order.class))).willReturn(Optional.of(payment));

        // when & then
        assertThrows(AlreadyPaidException.class, () -> {
            paymentService.requestTossPayment("testOrderNumber", paymentReqDto);
        });
    }

    @Test
    @DisplayName("Toss 결제 요청 - 결제 생성 실패")
    void requestTossPayment_PaymentCreationFailure_ThrowsException() {
        // given
        given(orderRepository.findByOrderNumber(anyString())).willReturn(Optional.of(order));
        given(paymentRepository.findByOrder(any(Order.class))).willReturn(Optional.empty());
        given(tossPaymentConfig.getSuccessUrl()).willReturn("http://localhost/success");
        given(tossPaymentConfig.getFailUrl()).willReturn("http://localhost/fail");
        given(tossPaymentClient.createPayment(anyMap())).willReturn(Map.of()); // Empty map simulates failure

        // when & then
        assertThrows(PaymentCreationException.class, () -> {
            paymentService.requestTossPayment("testOrderNumber", paymentReqDto);
        });
    }

    @Test
    @DisplayName("결제 성공 처리")
    void markSuccess_UpdatesPaymentAndOrderStatus() {
        // given
        PaymentApprovalRequestDto approvalDto = new PaymentApprovalRequestDto("testPaymentKey", "testOrderNumber", 15000L);
        given(paymentRepository.findByPaymentKey(anyString())).willReturn(Optional.of(payment));
        given(orderRepository.findByOrderNumber(anyString())).willReturn(Optional.of(order));
        given(tossPaymentClient.confirmPayment(any(PaymentApprovalRequestDto.class))).willReturn(approvalDto);

        // when
        paymentService.markSuccess(approvalDto);

        // then
        assertThat(payment.getPaymentStatus()).isEqualTo(PaymentStatus.SUCCESS);
        verify(order).setStatus(OrderStatus.PENDING_PAY);
        verify(paymentRepository).save(payment);
    }

    @Test
    @DisplayName("결제 실패 처리")
    void markFail_UpdatesPaymentStatus() {
        // given
        given(paymentRepository.findByPaymentKey(anyString())).willReturn(Optional.of(payment));

        // when
        paymentService.markFail("testPaymentKey", "Test failure reason");

        // then
        assertThat(payment.getPaymentStatus()).isEqualTo(PaymentStatus.FAIL);
        verify(paymentRepository).save(payment);
    }

    @Test
    @DisplayName("결제 실패 처리 - 결제 정보를 찾을 수 없음")
    void markFail_PaymentNotFound_ThrowsException() {
        // given
        given(paymentRepository.findByPaymentKey(anyString())).willReturn(Optional.empty());

        // when & then
        assertThrows(PaymentNotFoundException.class, () -> {
            paymentService.markFail("nonExistentKey", "Test failure reason");
        });
    }

    @Test
    @DisplayName("카드 결제 환불 - 성공")
    void refundCardPayment_Success() {
        // given
        given(paymentRepository.findByPaymentKey(anyString())).willReturn(Optional.of(payment));
        CancelPaymentRequest cancelRequest = new CancelPaymentRequest("testOrderNumber", 15000L, "Customer request");

        // when
        PaymentResDto result = paymentService.refundCardPayment("testPaymentKey", cancelRequest);

        // then
        assertThat(result).isNotNull();
        assertThat(payment.getPaymentStatus()).isEqualTo(PaymentStatus.CANCEL);
        verify(paymentRepository).save(payment);
    }

    @Test
    @DisplayName("카드 결제 환불 - FeignException 발생")
    void refundCardPayment_FeignException_ThrowsException() {
        // given
        given(paymentRepository.findByPaymentKey(anyString())).willReturn(Optional.of(payment));
        given(tossPaymentClient.cancelPayment(anyString(), anyMap())).willThrow(FeignException.class);
        CancelPaymentRequest cancelRequest = new CancelPaymentRequest("testOrderNumber", 15000L, "Customer request");

        // when & then
        assertThrows(FeignException.class, () -> {
            paymentService.refundCardPayment("testPaymentKey", cancelRequest);
        });
    }

    @Test
    @DisplayName("결제 정보 조회 - 성공")
    void getPaymentInfo_Success() {
        // given
        String paymentKey = "testPaymentKey";
        Map<String, Object> tossApiResponse = Map.of(
                "orderId", "testOrderNumber",
                "method", "CARD",
                "orderName", "Test Order",
                "amount", 15000,
                "nextRedirectPcUrl", "http://redirect.url"
        );
        given(tossPaymentClient.getPaymentInfo(paymentKey)).willReturn(tossApiResponse);
        given(tossPaymentConfig.getSuccessUrl()).willReturn("http://localhost/success");
        given(tossPaymentConfig.getFailUrl()).willReturn("http://localhost/fail");

        // when
        PaymentResDto result = paymentService.getPaymentInfo(paymentKey);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getPaymentKey()).isEqualTo(paymentKey);
        assertThat(result.getOrderId()).isEqualTo("testOrderNumber");
        assertThat(result.getPayType()).isEqualTo("CARD");
        assertThat(result.getPayName()).isEqualTo("Test Order");
        assertThat(result.getPayAmount()).isEqualTo(15000L);
        assertThat(result.getRedirectUrl()).isEqualTo("http://redirect.url");
        assertThat(result.getSuccessUrl()).isEqualTo("http://localhost/success");
        assertThat(result.getFailUrl()).isEqualTo("http://localhost/fail");
        verify(tossPaymentClient).getPaymentInfo(paymentKey);
    }

    @Test
    @DisplayName("결제 정보 조회 - amount가 Number 타입")
    void getPaymentInfo_AmountAsNumber() {
        // given
        String paymentKey = "testPaymentKey";
        Map<String, Object> tossApiResponse = Map.of(
                "orderId", "testOrderNumber",
                "method", "CARD",
                "orderName", "Test Order",
                "amount", Integer.valueOf(25000),
                "nextRedirectPcUrl", "http://redirect.url"
        );
        given(tossPaymentClient.getPaymentInfo(paymentKey)).willReturn(tossApiResponse);
        given(tossPaymentConfig.getSuccessUrl()).willReturn("http://localhost/success");
        given(tossPaymentConfig.getFailUrl()).willReturn("http://localhost/fail");

        // when
        PaymentResDto result = paymentService.getPaymentInfo(paymentKey);

        // then
        assertThat(result.getPayAmount()).isEqualTo(25000L);
    }

    @Test
    @DisplayName("결제 정보 조회 - 빈 값 처리")
    void getPaymentInfo_WithEmptyValues() {
        // given
        String paymentKey = "testPaymentKey";
        Map<String, Object> tossApiResponse = Map.of(
                "orderId", "",
                "method", "",
                "orderName", "",
                "amount", 0,
                "nextRedirectPcUrl", "http://redirect.url"
        );
        given(tossPaymentClient.getPaymentInfo(paymentKey)).willReturn(tossApiResponse);
        given(tossPaymentConfig.getSuccessUrl()).willReturn("http://localhost/success");
        given(tossPaymentConfig.getFailUrl()).willReturn("http://localhost/fail");

        // when
        PaymentResDto result = paymentService.getPaymentInfo(paymentKey);

        // then
        assertThat(result.getOrderId()).isEmpty();
        assertThat(result.getPayType()).isEmpty();
        assertThat(result.getPayName()).isEmpty();
        assertThat(result.getPayAmount()).isZero();
    }

    @Test
    @DisplayName("주문번호로 카드 결제 환불 - 성공")
    void refundCardPaymentByOrderNumber_Success() {
        // given
        String orderNumber = "testOrderNumber";
        String cancelReason = "Customer request";
        given(orderRepository.findByOrderNumber(orderNumber)).willReturn(Optional.of(order));
        given(paymentRepository.findByOrder(order)).willReturn(Optional.of(payment));

        // when
        PaymentResDto result = paymentService.refundCardPaymentByOrderNumber(orderNumber, cancelReason);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getPaymentKey()).isEqualTo("testPaymentKey");
        assertThat(result.getOrderId()).isEqualTo(orderNumber);
        assertThat(result.getPayType()).isEqualTo("CARD");
        assertThat(result.getPayName()).isEqualTo("Test Book");
        assertThat(result.getPayAmount()).isEqualTo(15000L);
        assertThat(payment.getPaymentStatus()).isEqualTo(PaymentStatus.CANCEL);
        verify(tossPaymentClient).cancelPayment(eq("testPaymentKey"), anyMap());
        verify(paymentRepository).save(payment);
    }

    @Test
    @DisplayName("주문번호로 카드 결제 환불 - 주문을 찾을 수 없음")
    void refundCardPaymentByOrderNumber_OrderNotFound() {
        // given
        String orderNumber = "nonExistentOrder";
        String cancelReason = "Customer request";
        given(orderRepository.findByOrderNumber(orderNumber)).willReturn(Optional.empty());

        // when & then
        assertThrows(OrderNotFoundException.class, () -> {
            paymentService.refundCardPaymentByOrderNumber(orderNumber, cancelReason);
        });
    }

    @Test
    @DisplayName("주문번호로 카드 결제 환불 - 결제 정보를 찾을 수 없음")
    void refundCardPaymentByOrderNumber_PaymentNotFound() {
        // given
        String orderNumber = "testOrderNumber";
        String cancelReason = "Customer request";
        given(orderRepository.findByOrderNumber(orderNumber)).willReturn(Optional.of(order));
        given(paymentRepository.findByOrder(order)).willReturn(Optional.empty());

        // when & then
        assertThrows(PaymentNotFoundException.class, () -> {
            paymentService.refundCardPaymentByOrderNumber(orderNumber, cancelReason);
        });
    }

    @Test
    @DisplayName("주문번호로 카드 결제 환불 - Toss API 호출 확인")
    void refundCardPaymentByOrderNumber_TossApiCall() {
        // given
        String orderNumber = "testOrderNumber";
        String cancelReason = "Customer request";
        given(orderRepository.findByOrderNumber(orderNumber)).willReturn(Optional.of(order));
        given(paymentRepository.findByOrder(order)).willReturn(Optional.of(payment));

        // when
        paymentService.refundCardPaymentByOrderNumber(orderNumber, cancelReason);

        // then
        Map<String, Object> expectedCancelRequest = Map.of(
                "cancelReason", cancelReason,
                "cancelAmount", 15000L
        );
        verify(tossPaymentClient).cancelPayment("testPaymentKey", expectedCancelRequest);
    }
}


