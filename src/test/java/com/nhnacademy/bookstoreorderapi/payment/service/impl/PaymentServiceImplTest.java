package com.nhnacademy.bookstoreorderapi.payment.service.impl;

import com.nhnacademy.bookstoreorderapi.common.exception.OrderNotFoundException;
import com.nhnacademy.bookstoreorderapi.order.domain.entity.Order;
import com.nhnacademy.bookstoreorderapi.order.domain.entity.OrderStatus;
import com.nhnacademy.bookstoreorderapi.order.repository.OrderRepository;
import com.nhnacademy.bookstoreorderapi.payment.client.TossPaymentClient;
import com.nhnacademy.bookstoreorderapi.payment.config.TossPaymentConfig;
import com.nhnacademy.bookstoreorderapi.payment.domain.PayType;
import com.nhnacademy.bookstoreorderapi.payment.domain.PaymentStatus;
import com.nhnacademy.bookstoreorderapi.payment.domain.entity.Payment;
import com.nhnacademy.bookstoreorderapi.payment.dto.Request.CancelPaymentRequest;
import com.nhnacademy.bookstoreorderapi.payment.dto.Request.PaymentReqDto;
import com.nhnacademy.bookstoreorderapi.payment.dto.Response.PaymentResDto;
import com.nhnacademy.bookstoreorderapi.payment.exception.AlreadyPaidException;
import com.nhnacademy.bookstoreorderapi.payment.exception.PaymentCreationException;
import com.nhnacademy.bookstoreorderapi.payment.exception.PaymentNotFoundException;
import com.nhnacademy.bookstoreorderapi.payment.repository.PaymentRepository;
import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock
    private OrderRepository orderRepo;

    @Mock
    private PaymentRepository payRepo;

    @Mock
    private TossPaymentConfig tossProps;

    @Mock
    private TossPaymentClient tossClient;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    @Mock
    private Order order; // Use Mock for Order entity

    private Payment payment;
    private PaymentReqDto paymentReqDto;

    @BeforeEach
    void setUp() {
        // Setup mocked order
        lenient().when(order.getOrderId()).thenReturn("testOrderId");

        // Setup payment entity
        payment = new Payment();
        payment.setPaymentKey("testPaymentKey");
        payment.setOrder(order);
        payment.setPayAmount(10000L);
        payment.setPayType(PayType.CARD);
        payment.setPayName("Test Payment");

        // Setup DTO
        paymentReqDto = new PaymentReqDto();
        paymentReqDto.setPayType(PayType.CARD);
        paymentReqDto.setPayAmount(10000L);
        paymentReqDto.setPayName("Test Payment");
    }

    @Test
    void requestTossPayment_Success() {
        when(orderRepo.findByOrderId("testOrderId")).thenReturn(Optional.of(order));
        when(payRepo.findByOrder(order)).thenReturn(Optional.empty());
        when(tossProps.getSuccessUrl()).thenReturn("successUrl");
        when(tossProps.getFailUrl()).thenReturn("failUrl");
        when(tossClient.createPayment(anyMap())).thenReturn(Map.of("paymentKey", "newPaymentKey", "nextRedirectPcUrl", "redirectUrl"));

        PaymentResDto result = paymentService.requestTossPayment("testOrderId", paymentReqDto);

        assertNotNull(result);
        assertEquals("newPaymentKey", result.getPaymentKey());
        assertEquals("redirectUrl", result.getRedirectUrl());
        verify(payRepo).save(any(Payment.class));
    }

    @Test
    void requestTossPayment_OrderNotFound() {
        when(orderRepo.findByOrderId("testOrderId")).thenReturn(Optional.empty());
        assertThrows(OrderNotFoundException.class, () -> paymentService.requestTossPayment("testOrderId", paymentReqDto));
    }

    @Test
    void requestTossPayment_AlreadyPaid() {
        payment.setPaymentStatus(PaymentStatus.SUCCESS);
        when(orderRepo.findByOrderId("testOrderId")).thenReturn(Optional.of(order));
        when(payRepo.findByOrder(order)).thenReturn(Optional.of(payment));
        assertThrows(AlreadyPaidException.class, () -> paymentService.requestTossPayment("testOrderId", paymentReqDto));
    }

    @Test
    void requestTossPayment_PaymentCreationException() {
        when(orderRepo.findByOrderId("testOrderId")).thenReturn(Optional.of(order));
        when(payRepo.findByOrder(order)).thenReturn(Optional.empty());
        when(tossProps.getSuccessUrl()).thenReturn("https://example.com/success");
        when(tossProps.getFailUrl()).thenReturn("https://example.com/fail");
        when(tossClient.createPayment(anyMap())).thenReturn(Map.of()); // No paymentKey
        assertThrows(PaymentCreationException.class, () -> paymentService.requestTossPayment("testOrderId", paymentReqDto));
    }

    @Test
    void markSuccess_Success() {
        when(orderRepo.findByOrderId("testOrderId")).thenReturn(Optional.of(order));
        when(payRepo.findByOrder(order)).thenReturn(Optional.of(payment));
        // Mock the confirmPayment call
        doNothing().when(tossClient).confirmPayment(anyString(), anyMap());

        paymentService.markSuccess("testPaymentKey", "testOrderId", 10000L);

        assertEquals(PaymentStatus.SUCCESS, payment.getPaymentStatus());
        verify(payRepo).save(payment);
        verify(order).setStatus(OrderStatus.PENDING);
    }

    @Test
    void markSuccess_FeignExceptionNotFound() {
        when(orderRepo.findByOrderId("testOrderId")).thenReturn(Optional.of(order));
        when(payRepo.findByOrder(order)).thenReturn(Optional.of(payment));
        // Mock the confirmPayment call to throw a 404 FeignException
        FeignException.NotFound notFoundException = mock(FeignException.NotFound.class);
        doThrow(notFoundException).when(tossClient).confirmPayment(anyString(), anyMap());

        paymentService.markSuccess("testPaymentKey", "testOrderId", 10000L);

        assertEquals(PaymentStatus.SUCCESS, payment.getPaymentStatus());
        verify(payRepo).save(payment);
        verify(order).setStatus(OrderStatus.PENDING);
    }

    @Test
    void markFail_Success() {
        when(payRepo.findByPaymentKey("testPaymentKey")).thenReturn(Optional.of(payment));
        paymentService.markFail("testPaymentKey", "Test Reason");
        assertEquals(PaymentStatus.FAIL, payment.getPaymentStatus());
        verify(payRepo).save(payment);
    }

    @Test
    void markFail_PaymentNotFound() {
        when(payRepo.findByPaymentKey("testPaymentKey")).thenReturn(Optional.empty());
        assertThrows(PaymentNotFoundException.class, () -> paymentService.markFail("testPaymentKey", "Test Reason"));
    }

    @Test
    void refundCardPayment_Success() {
        when(payRepo.findByPaymentKey("testPaymentKey")).thenReturn(Optional.of(payment));
        // Mock the cancelPayment call
        when(tossClient.cancelPayment(anyString(), anyMap())).thenReturn(Map.of());
        CancelPaymentRequest cancelRequest = new CancelPaymentRequest("testOrderId", 10000L, "Cancel Reason");

        PaymentResDto result = paymentService.refundCardPayment("testPaymentKey", cancelRequest);

        assertNotNull(result);
        assertEquals(PaymentStatus.CANCEL, payment.getPaymentStatus());
        verify(payRepo).save(payment);
    }

    @Test
    void getPaymentInfo_Success() {
        when(tossClient.getPaymentInfo(anyString())).thenReturn(Map.of(
                "orderId", "testOrderId",
                "method", "CARD",
                "orderName", "Test Payment",
                "amount", 10000,
                "nextRedirectPcUrl", "redirectUrl"
        ));
        when(tossProps.getSuccessUrl()).thenReturn("successUrl");
        when(tossProps.getFailUrl()).thenReturn("failUrl");

        PaymentResDto result = paymentService.getPaymentInfo("testPaymentKey");

        assertNotNull(result);
        assertEquals("testOrderId", result.getOrderId());
        assertEquals("CARD", result.getPayType());
        assertEquals(10000L, result.getPayAmount());
    }

    @Test
    void refundCardPayment_MapVersion_Success() {
        when(payRepo.findByPaymentKey("testPaymentKey")).thenReturn(Optional.of(payment));
        when(tossClient.cancelPayment(anyString(), anyMap())).thenReturn(Map.of());
        Map<String, Object> request = Map.of(
            "orderId", "testOrderId",
            "amount", 10000L,
            "cancelReason", "Cancel Reason"
        );

        Map<String, Object> response = paymentService.refundCardPayment("testPaymentKey", request);

        assertNotNull(response);
        assertTrue(response.containsKey("data"));
        PaymentResDto result = (PaymentResDto) response.get("data");
        assertEquals(PaymentStatus.CANCEL, payment.getPaymentStatus());
        assertEquals("testPaymentKey", result.getPaymentKey());
        verify(payRepo).save(payment);
    }
}
