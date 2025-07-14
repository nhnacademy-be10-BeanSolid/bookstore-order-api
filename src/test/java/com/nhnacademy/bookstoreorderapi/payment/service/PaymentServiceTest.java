package com.nhnacademy.bookstoreorderapi.payment.service;

import com.nhnacademy.bookstoreorderapi.order.domain.entity.Order;
import com.nhnacademy.bookstoreorderapi.order.exception.notfound.OrderNotFoundException;
import com.nhnacademy.bookstoreorderapi.order.repository.OrderRepository;
import com.nhnacademy.bookstoreorderapi.payment.client.TossPaymentClient;
import com.nhnacademy.bookstoreorderapi.payment.config.TossPaymentConfig;
import com.nhnacademy.bookstoreorderapi.payment.domain.PayType;
import com.nhnacademy.bookstoreorderapi.payment.domain.entity.Payment;
import com.nhnacademy.bookstoreorderapi.payment.domain.PaymentStatus;
import com.nhnacademy.bookstoreorderapi.payment.dto.Request.PaymentApprovalRequestDto;
import com.nhnacademy.bookstoreorderapi.payment.dto.Request.CancelPaymentRequest;
import com.nhnacademy.bookstoreorderapi.payment.dto.Request.PaymentReqDto;
import com.nhnacademy.bookstoreorderapi.payment.dto.Response.PaymentResDto;
import com.nhnacademy.bookstoreorderapi.payment.exception.AlreadyPaidException;
import com.nhnacademy.bookstoreorderapi.payment.exception.PaymentConfirmationException;
import com.nhnacademy.bookstoreorderapi.payment.exception.PaymentCreationException;
import com.nhnacademy.bookstoreorderapi.payment.exception.PaymentNotFoundException;
import com.nhnacademy.bookstoreorderapi.payment.exception.RedirectUrlNotFoundException;
import com.nhnacademy.bookstoreorderapi.payment.repository.PaymentRepository;
import com.nhnacademy.bookstoreorderapi.payment.service.impl.PaymentServiceImpl;
import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.quality.Strictness;
import org.mockito.junit.jupiter.MockitoSettings;

import java.lang.reflect.Method;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PaymentServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private TossPaymentConfig tossPaymentConfig;

    @Mock
    private TossPaymentClient tossPaymentClient;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    @Mock
    private Order order;

    private PaymentReqDto paymentReqDto;
    private Payment payment;

    @BeforeEach
    void setUp() {
        lenient().when(order.getOrderNumber()).thenReturn("testOrderId");

        paymentReqDto = new PaymentReqDto();
        paymentReqDto.setPayType(PayType.CARD);
        paymentReqDto.setPayName("Test Payment");
        paymentReqDto.setPayAmount(1000L);

        payment = new Payment();
        payment.setPaymentKey("testPaymentKey");
        payment.setOrder(order);
        payment.setPayType(PayType.CARD);
        payment.setPayAmount(1000L);
        payment.setPayName("Test Payment");
        payment.setPaymentStatus(PaymentStatus.PENDING);
    }

    @Test
    void requestTossPayment_Success() {
        when(orderRepository.findByOrderNumber("testOrderId")).thenReturn(Optional.of(order));
        when(paymentRepository.findByOrder(order)).thenReturn(Optional.empty());
        when(tossPaymentConfig.getSuccessUrl()).thenReturn("http://localhost/success");
        when(tossPaymentConfig.getFailUrl()).thenReturn("http://localhost/fail");
        when(tossPaymentClient.createPayment(any())).thenReturn(Map.of("paymentKey", "newPaymentKey", "nextRedirectPcUrl", "http://localhost/redirect"));

        PaymentResDto result = paymentService.requestTossPayment("testOrderId", paymentReqDto);

        assertNotNull(result);
        assertEquals("newPaymentKey", result.getPaymentKey());
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    void requestTossPayment_OrderNotFound() {
        when(orderRepository.findByOrderNumber("testOrderId")).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class, () -> paymentService.requestTossPayment("testOrderId", paymentReqDto));
    }

    @Test
    void requestTossPayment_AlreadyPaid() {
        payment.setPaymentStatus(PaymentStatus.SUCCESS);
        when(orderRepository.findByOrderNumber("testOrderId")).thenReturn(Optional.of(order));
        when(paymentRepository.findByOrder(order)).thenReturn(Optional.of(payment));

        assertThrows(AlreadyPaidException.class, () -> paymentService.requestTossPayment("testOrderId", paymentReqDto));
    }

    @Test
    void requestTossPayment_PaymentKeyIsBlank() {
        when(orderRepository.findByOrderNumber("testOrderId")).thenReturn(Optional.of(order));
        when(paymentRepository.findByOrder(order)).thenReturn(Optional.empty());
        when(tossPaymentConfig.getSuccessUrl()).thenReturn("http://localhost/success");
        when(tossPaymentConfig.getFailUrl()).thenReturn("http://localhost/fail");
        when(tossPaymentClient.createPayment(any())).thenReturn(Map.of("paymentKey", "", "nextRedirectPcUrl", "http://localhost/redirect"));

        assertThrows(PaymentCreationException.class, () -> paymentService.requestTossPayment("testOrderId", paymentReqDto));
    }

    @Test
    void requestTossPayment_PayTypeAccount() {
        paymentReqDto.setPayType(PayType.ACCOUNT);
        when(orderRepository.findByOrderNumber("testOrderId")).thenReturn(Optional.of(order));
        when(paymentRepository.findByOrder(order)).thenReturn(Optional.empty());
        when(tossPaymentConfig.getSuccessUrl()).thenReturn("http://localhost/success");
        when(tossPaymentConfig.getFailUrl()).thenReturn("http://localhost/fail");
        when(tossPaymentClient.createPayment(any())).thenReturn(Map.of("paymentKey", "newPaymentKey", "nextRedirectPcUrl", "http://localhost/redirect"));

        PaymentResDto result = paymentService.requestTossPayment("testOrderId", paymentReqDto);

        assertNotNull(result);
        assertEquals("newPaymentKey", result.getPaymentKey());
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    void requestTossPayment_ReuseExistingPayment() {
        when(orderRepository.findByOrderNumber("testOrderId")).thenReturn(Optional.of(order));
        when(paymentRepository.findByOrder(order)).thenReturn(Optional.of(payment)); // Return existing payment
        when(tossPaymentConfig.getSuccessUrl()).thenReturn("http://localhost/success");
        when(tossPaymentConfig.getFailUrl()).thenReturn("http://localhost/fail");
        when(tossPaymentClient.createPayment(any())).thenReturn(Map.of("paymentKey", "newPaymentKey", "nextRedirectPcUrl", "http://localhost/redirect"));

        PaymentResDto result = paymentService.requestTossPayment("testOrderId", paymentReqDto);

        assertNotNull(result);
        assertEquals("newPaymentKey", result.getPaymentKey());
        verify(paymentRepository).save(payment); // Verify that the existing payment object is saved
    }

    @Test
    void markSuccess_Success() {
        when(orderRepository.findByOrderNumber("testOrderId")).thenReturn(Optional.of(order));
        when(paymentRepository.findByPaymentKey("testPaymentKey")).thenReturn(Optional.of(payment));
        when(tossPaymentClient.confirmPayment(any(PaymentApprovalRequestDto.class))).thenReturn(mock(PaymentApprovalRequestDto.class));

        paymentService.markSuccess("testPaymentKey", "testOrderId", 1000L);

        assertEquals(PaymentStatus.SUCCESS, payment.getPaymentStatus());
        verify(paymentRepository).save(payment);
    }

    @Test
    void markSuccess_FeignExceptionNotFound() {
        doThrow(mock(FeignException.NotFound.class)).when(tossPaymentClient).confirmPayment(any(PaymentApprovalRequestDto.class));
        when(orderRepository.findByOrderNumber("testOrderId")).thenReturn(Optional.of(order));
        when(paymentRepository.findByPaymentKey("testPaymentKey")).thenReturn(Optional.of(payment));

        paymentService.markSuccess("testPaymentKey", "testOrderId", 1000L);

        assertEquals(PaymentStatus.SUCCESS, payment.getPaymentStatus()); // Status should still be success if it was already set
        verify(paymentRepository).save(payment);
    }

    @Test
    void markSuccess_FeignException() {
        doThrow(mock(FeignException.class)).when(tossPaymentClient).confirmPayment(any(PaymentApprovalRequestDto.class));
        when(orderRepository.findByOrderNumber("testOrderId")).thenReturn(Optional.of(order));
        when(paymentRepository.findByPaymentKey("testPaymentKey")).thenReturn(Optional.of(payment));

        assertThrows(PaymentConfirmationException.class, () -> paymentService.markSuccess("testPaymentKey", "testOrderId", 1000L));
    }

    @Test
    void markSuccess_OrderNotFound() {
        when(tossPaymentClient.confirmPayment(any(PaymentApprovalRequestDto.class))).thenReturn(mock(PaymentApprovalRequestDto.class));
        when(paymentRepository.findByPaymentKey("testPaymentKey")).thenReturn(Optional.of(payment));
        when(orderRepository.findByOrderNumber("testOrderId")).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class, () -> paymentService.markSuccess("testPaymentKey", "testOrderId", 1000L));
    }

    @Test
    void markSuccess_PaymentNotFound() {
        when(tossPaymentClient.confirmPayment(any(PaymentApprovalRequestDto.class))).thenReturn(mock(PaymentApprovalRequestDto.class));
        when(orderRepository.findByOrderNumber("testOrderId")).thenReturn(Optional.of(order));
        when(paymentRepository.findByPaymentKey("testPaymentKey")).thenReturn(Optional.empty());

        assertThrows(PaymentNotFoundException.class, () -> paymentService.markSuccess("testPaymentKey", "testOrderId", 1000L));
    }

    @Test
    void markFail_Success() {
        when(paymentRepository.findByPaymentKey("testPaymentKey")).thenReturn(Optional.of(payment));

        paymentService.markFail("testPaymentKey", "Test Reason");

        assertEquals(PaymentStatus.FAIL, payment.getPaymentStatus());
        verify(paymentRepository).save(payment);
    }

    @Test
    void markFail_PaymentNotFound() {
        when(paymentRepository.findByPaymentKey("testPaymentKey")).thenReturn(Optional.empty());

        assertThrows(PaymentNotFoundException.class, () -> paymentService.markFail("testPaymentKey", "Test Reason"));
    }

    @Test
    void refundCardPayment_Success() {
        CancelPaymentRequest cancelPaymentRequest = new CancelPaymentRequest("testOrderId", 1000L, "Test Cancel");
        when(paymentRepository.findByPaymentKey("testPaymentKey")).thenReturn(Optional.of(payment));

        PaymentResDto result = paymentService.refundCardPayment("testPaymentKey", cancelPaymentRequest);

        assertNotNull(result);
        assertEquals(PaymentStatus.CANCEL, payment.getPaymentStatus());
        verify(paymentRepository).save(payment);
        verify(tossPaymentClient).cancelPayment(eq("testPaymentKey"), any());
    }

    @Test
    void refundCardPayment_PaymentNotFound() {
        CancelPaymentRequest cancelPaymentRequest = new CancelPaymentRequest("testOrderId", 1000L, "Test Cancel");
        when(paymentRepository.findByPaymentKey("testPaymentKey")).thenReturn(Optional.empty());

        assertThrows(PaymentNotFoundException.class, () -> paymentService.refundCardPayment("testPaymentKey", cancelPaymentRequest));
    }

    @Test
    void getPaymentInfo_Success() {
        when(tossPaymentClient.getPaymentInfo("testPaymentKey")).thenReturn(Map.of(
                "orderId", "testOrderId",
                "method", "CARD",
                "orderName", "Test Payment",
                "amount", 1000,
                "nextRedirectPcUrl", "http://localhost/redirect"
        ));

        PaymentResDto result = paymentService.getPaymentInfo("testPaymentKey");

        assertNotNull(result);
        assertEquals("testOrderId", result.getOrderId());
    }

    @Test
    void getPaymentInfo_RedirectUrlNotFound() {
        when(tossPaymentClient.getPaymentInfo("testPaymentKey")).thenReturn(Map.of(
                "orderId", "testOrderId",
                "method", "CARD",
                "orderName", "Test Payment",
                "amount", 1000
        )); // No redirect URL in response

        assertThrows(RedirectUrlNotFoundException.class, () -> paymentService.getPaymentInfo("testPaymentKey"));
    }

    @Test
    void refundCardPayment_MapVersion_Success() {
        Map<String, Object> requestMap = Map.of(
                "orderId", "testOrderId",
                "amount", 1000L,
                "cancelReason", "Test Cancel Map"
        );
        when(paymentRepository.findByPaymentKey("testPaymentKey")).thenReturn(Optional.of(payment));
        when(tossPaymentClient.cancelPayment(eq("testPaymentKey"), any())).thenReturn(Map.of("status", "CANCELED"));

        Map<String, Object> result = paymentService.refundCardPayment("testPaymentKey", requestMap);

        assertNotNull(result);
        assertTrue(result.containsKey("data"));
        assertEquals(PaymentStatus.CANCEL, payment.getPaymentStatus());
        verify(paymentRepository).save(payment);
        verify(tossPaymentClient).cancelPayment(eq("testPaymentKey"), any());
    }

    @Test
    void extractRedirectUrl_VariousUrls() throws Exception {
        PaymentServiceImpl spyService = spy(paymentService);
        Method extractRedirectUrlMethod = PaymentServiceImpl.class.getDeclaredMethod("extractRedirectUrl", Map.class);
        extractRedirectUrlMethod.setAccessible(true);

        Map<String, Object> resp1 = Map.of("nextRedirectPcUrl", "pcUrl");
        assertEquals("pcUrl", extractRedirectUrlMethod.invoke(spyService, resp1));

        Map<String, Object> resp2 = Map.of("nextRedirectMobileUrl", "mobileUrl");
        assertEquals("mobileUrl", extractRedirectUrlMethod.invoke(spyService, resp2));

        Map<String, Object> resp3 = Map.of("hostedCheckoutUrl", "hostedUrl");
        assertEquals("hostedUrl", extractRedirectUrlMethod.invoke(spyService, resp3));

        Map<String, Object> resp4 = Map.of("checkoutUrl", "checkoutUrl");
        assertEquals("checkoutUrl", extractRedirectUrlMethod.invoke(spyService, resp4));

        Map<String, Object> resp5 = Map.of("checkoutPageUrl", "checkoutPageUrl");
        assertEquals("checkoutPageUrl", extractRedirectUrlMethod.invoke(spyService, resp5));

        Map<String, Object> resp6 = Map.of("paymentUrl", "paymentUrl");
        assertEquals("paymentUrl", extractRedirectUrlMethod.invoke(spyService, resp6));

        Map<String, Object> resp7 = Map.of("checkout", Map.of("url", "nestedCheckoutUrl"));
        assertEquals("nestedCheckoutUrl", extractRedirectUrlMethod.invoke(spyService, resp7));
    }

    @Test
    void extractRedirectUrl_NotFound() throws Exception {
        PaymentServiceImpl spyService = spy(paymentService);
        Method extractRedirectUrlMethod = PaymentServiceImpl.class.getDeclaredMethod("extractRedirectUrl", Map.class);
        extractRedirectUrlMethod.setAccessible(true);

        Map<String, Object> resp = Map.of("someOtherKey", "someValue");
        InvocationTargetException thrown = assertThrows(InvocationTargetException.class, () -> extractRedirectUrlMethod.invoke(spyService, resp));
        assertTrue(thrown.getCause() instanceof RedirectUrlNotFoundException);
    }
}
