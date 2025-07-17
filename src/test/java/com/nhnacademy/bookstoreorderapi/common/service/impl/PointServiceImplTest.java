package com.nhnacademy.bookstoreorderapi.common.service.impl;

import com.nhnacademy.bookstoreorderapi.order.client.user.UserServiceClient;
import com.nhnacademy.bookstoreorderapi.order.client.user.dto.request.OrderPointMinusProcessRequest;
import com.nhnacademy.bookstoreorderapi.order.client.user.dto.request.OrderPointPlusProcessRequest;
import com.nhnacademy.bookstoreorderapi.order.client.user.dto.request.PointType;
import com.nhnacademy.bookstoreorderapi.order.client.user.dto.response.ResponsePointType;
import com.nhnacademy.bookstoreorderapi.order.domain.Order;
import com.nhnacademy.bookstoreorderapi.payment.domain.entity.Payment;
import com.nhnacademy.bookstoreorderapi.payment.exception.NonMemberPointUsageAttemptException;
import com.nhnacademy.bookstoreorderapi.payment.exception.NotEnoughPointException;
import com.nhnacademy.bookstoreorderapi.payment.repository.PaymentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PointServiceImplTest {

    @Mock
    private UserServiceClient userServiceClient;

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private PointServiceImpl pointService;

    @Test
    @DisplayName("사용자가 존재하고 적립 포인트가 양수일 때 포인트가 적립되어야 한다")
    void processEarnedPoints_shouldAddPointsWhenUserExistsAndEarnedPointIsPositive() {
        Order order = mock(Order.class);
        Payment payment = mock(Payment.class);
        ResponsePointType responsePointType = mock(ResponsePointType.class);

        when(order.getUserNo()).thenReturn(1L);
        when(payment.getPayAmount()).thenReturn(10000L);
        when(responsePointType.getEarningRate()).thenReturn(5);
        when(userServiceClient.getEarningRateByUserNo(anyLong())).thenReturn(responsePointType);

        pointService.processEarnedPoints(order, payment);

        verify(userServiceClient, times(1)).orderPointPlusProcess(eq(1L), any(OrderPointPlusProcessRequest.class));
    }

    @Test
    @DisplayName("사용자가 존재하지 않을 때 포인트가 적립되지 않아야 한다")
    void processEarnedPoints_shouldNotAddPointsWhenUserDoesNotExist() {
        Order order = mock(Order.class);
        Payment payment = mock(Payment.class);

        when(order.getUserNo()).thenReturn(null);

        pointService.processEarnedPoints(order, payment);

        verify(userServiceClient, never()).orderPointPlusProcess(anyLong(), any(OrderPointPlusProcessRequest.class));
    }

    @Test
    @DisplayName("적립 포인트가 0일 때 포인트가 적립되지 않아야 한다")
    void processEarnedPoints_shouldNotAddPointsWhenEarnedPointIsZero() {
        Order order = mock(Order.class);
        Payment payment = mock(Payment.class);
        ResponsePointType responsePointType = mock(ResponsePointType.class);

        when(order.getUserNo()).thenReturn(1L);
        when(payment.getPayAmount()).thenReturn(0L);
        when(responsePointType.getEarningRate()).thenReturn(5);
        when(userServiceClient.getEarningRateByUserNo(anyLong())).thenReturn(responsePointType);

        pointService.processEarnedPoints(order, payment);

        verify(userServiceClient, never()).orderPointPlusProcess(anyLong(), any(OrderPointPlusProcessRequest.class));
    }

    @Test
    @DisplayName("사용 포인트가 양수일 때 포인트가 차감되어야 한다")
    void processUsedPoints_shouldDeductPointsWhenUsedPointIsPositive() {
        Order order = mock(Order.class);
        Payment payment = mock(Payment.class);

        when(order.getId()).thenReturn(1L);
        when(payment.getUsedPoint()).thenReturn(1000);

        pointService.processUsedPoints(order, payment);

        verify(userServiceClient, times(1)).orderPointMinusProcess(anyLong(), any(OrderPointMinusProcessRequest.class));
    }

    @Test
    @DisplayName("사용 포인트가 0일 때 포인트가 차감되지 않아야 한다")
    void processUsedPoints_shouldNotDeductPointsWhenUsedPointIsZero() {
        Order order = mock(Order.class);
        Payment payment = mock(Payment.class);

        when(payment.getUsedPoint()).thenReturn(0);

        pointService.processUsedPoints(order, payment);

        verify(userServiceClient, never()).orderPointMinusProcess(anyLong(), any(OrderPointMinusProcessRequest.class));
    }

    @Test
    @DisplayName("사용 포인트가 0일 때 유효성 검사를 통과해야 한다")
    void validatePointUsage_shouldPassWhenUsedPointIsZero() {
        assertDoesNotThrow(() -> pointService.validatePointUsage(1L, 0));
        verify(userServiceClient, never()).getUserPointByUserNo(anyLong());
    }

    @Test
    @DisplayName("비회원이 포인트를 사용하려 할 때 NonMemberPointUsageAttemptException을 던져야 한다")
    void validatePointUsage_shouldThrowNonMemberPointUsageAttemptExceptionWhenNonMemberUsesPoint() {
        assertThrows(NonMemberPointUsageAttemptException.class, () ->
                pointService.validatePointUsage(null, 100));
        verify(userServiceClient, never()).getUserPointByUserNo(anyLong());
    }

    @Test
    @DisplayName("포인트가 부족할 때 NotEnoughPointException을 던져야 한다")
    void validatePointUsage_shouldThrowNotEnoughPointExceptionWhenNotEnoughPoint() {
        when(userServiceClient.getUserPointByUserNo(anyLong())).thenReturn(50);

        assertThrows(NotEnoughPointException.class, () ->
                pointService.validatePointUsage(1L, 100));
        verify(userServiceClient, times(1)).getUserPointByUserNo(1L);
    }

    @Test
    @DisplayName("포인트가 충분할 때 유효성 검사를 통과해야 한다")
    void validatePointUsage_shouldPassWhenEnoughPoint() {
        when(userServiceClient.getUserPointByUserNo(anyLong())).thenReturn(200);

        assertDoesNotThrow(() -> pointService.validatePointUsage(1L, 100));
        verify(userServiceClient, times(1)).getUserPointByUserNo(1L);
    }

    @Test
    @DisplayName("사용자가 존재하고 환불 금액이 양수일 때 포인트가 환불되어야 한다")
    void processPointRefund_shouldRefundPointsWhenUserExistsAndRefundAmountIsPositive() {
        Order order = mock(Order.class);
        Payment payment = mock(Payment.class);

        when(order.getUserNo()).thenReturn(1L);
        when(order.getId()).thenReturn(1L);
        when(payment.getUsedPoint()).thenReturn(500);
        when(paymentRepository.findByOrder(any(Order.class))).thenReturn(Optional.of(payment));

        pointService.processPointRefund(order, 1000L);

        verify(userServiceClient, times(2)).orderPointPlusProcess(eq(1L), any(OrderPointPlusProcessRequest.class));
    }

    @Test
    @DisplayName("사용자가 존재하지 않을 때 포인트가 환불되지 않아야 한다")
    void processPointRefund_shouldNotRefundPointsWhenUserDoesNotExist() {
        Order order = mock(Order.class);

        when(order.getUserNo()).thenReturn(null);

        pointService.processPointRefund(order, 1000L);

        verify(userServiceClient, never()).orderPointPlusProcess(anyLong(), any(OrderPointPlusProcessRequest.class));
    }

    @Test
    @DisplayName("사용된 포인트가 0일 때 사용된 포인트가 환불되지 않아야 한다")
    void processPointRefund_shouldNotRefundUsedPointsWhenUsedPointIsZero() {
        Order order = mock(Order.class);
        Payment payment = mock(Payment.class);

        when(order.getUserNo()).thenReturn(1L);
        when(order.getId()).thenReturn(1L);
        when(payment.getUsedPoint()).thenReturn(0);
        when(paymentRepository.findByOrder(any(Order.class))).thenReturn(Optional.of(payment));

        pointService.processPointRefund(order, 1000L);

        verify(userServiceClient, times(1)).orderPointPlusProcess(eq(1L), any(OrderPointPlusProcessRequest.class)); // Only for refundAmount
        verify(userServiceClient, never()).orderPointPlusProcess(eq(1L), argThat(req -> req.pointType() == PointType.RETURN && req.point() == 0));
    }

    @Test
    @DisplayName("결제 정보를 찾을 수 없을 때 사용된 포인트가 환불되지 않아야 한다")
    void processPointRefund_shouldNotRefundUsedPointsWhenPaymentNotFound() {
        Order order = mock(Order.class);

        when(order.getUserNo()).thenReturn(1L);
        when(order.getId()).thenReturn(1L);
        when(paymentRepository.findByOrder(any(Order.class))).thenReturn(Optional.empty());

        pointService.processPointRefund(order, 1000L);

        verify(userServiceClient, times(1)).orderPointPlusProcess(eq(1L), any(OrderPointPlusProcessRequest.class)); // Only for refundAmount
    }
}