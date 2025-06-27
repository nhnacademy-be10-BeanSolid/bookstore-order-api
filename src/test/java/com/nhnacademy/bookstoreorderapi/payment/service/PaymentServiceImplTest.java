package com.nhnacademy.bookstoreorderapi.payment.service;

import com.nhnacademy.bookstoreorderapi.order.domain.entity.Order;
import com.nhnacademy.bookstoreorderapi.order.repository.OrderRepository;
import com.nhnacademy.bookstoreorderapi.payment.config.TossPaymentConfig;
import com.nhnacademy.bookstoreorderapi.payment.domain.PayType;
import com.nhnacademy.bookstoreorderapi.payment.domain.PaymentStatus;
import com.nhnacademy.bookstoreorderapi.payment.domain.entity.Payment;
import com.nhnacademy.bookstoreorderapi.payment.dto.Request.PaymentReqDto;
import com.nhnacademy.bookstoreorderapi.payment.dto.Response.PaymentResDto;
import com.nhnacademy.bookstoreorderapi.payment.repository.PaymentRepository;
import com.nhnacademy.bookstoreorderapi.payment.service.impl.PaymentServiceImpl;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.Optional;

import static org.mockito.Mockito.*;

class PaymentServiceImplTest {

    RestTemplate      restTemplate = mock(RestTemplate.class);
    OrderRepository   orderRepo    = mock(OrderRepository.class);
    PaymentRepository payRepo      = mock(PaymentRepository.class);
    TossPaymentConfig tossProps    = new TossPaymentConfig();

    PaymentServiceImpl sut;

    @BeforeEach
    void setUp() {
        // dummy config
        tossProps.setSecretApiKey("dummy");
        tossProps.setSuccessUrl("http://suc");
        tossProps.setFailUrl("http://fail");
        sut = new PaymentServiceImpl(orderRepo, payRepo, tossProps, restTemplate);
    }

    @Test
    @DisplayName("requestTossPayment 성공 플로우")
    void requestTossPayment_ok() {
        String orderId = "ORD-100";
        Order dummyOrder = mock(Order.class);
        when(orderRepo.findByOrderId(orderId)).thenReturn(dummyOrder);
        when(payRepo.findByOrder(dummyOrder)).thenReturn(Optional.empty());

        Map<String,Object> fakeResp = Map.of(
                "paymentKey",  "payKey-abc",
                "checkoutUrl", "https://toss.test"
        );
        when(restTemplate.exchange(
                eq(TossPaymentConfig.PAYMENTS_URL),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                ArgumentMatchers.<ParameterizedTypeReference<Map<String,Object>>>any()
        )).thenReturn(new ResponseEntity<>(fakeResp, HttpStatus.OK));

        PaymentReqDto req = PaymentReqDto.builder()
                .payAmount(10_000L)
                .payType(PayType.CARD)
                .payName("테스트")
                .build();

        PaymentResDto res = sut.requestTossPayment(orderId, req);

        Assertions.assertThat(res.getPaymentKey()).isEqualTo("payKey-abc");
        verify(orderRepo).findByOrderId(orderId);
        verify(payRepo).findByOrder(dummyOrder);
    }

    @Test
    @DisplayName("markSuccess: 성공 콜백 후 저장로직 호출")
    void markSuccess_ok() {
        String orderId = "O-1";
        String payKey = "p-1";
        long amount = 5_000L;
        Order o = mock(Order.class);
        when(orderRepo.findByOrderId(orderId)).thenReturn(o);
        when(payRepo.findByOrder(o)).thenReturn(Optional.empty());

        // mocking Toss confirm API (void)
        when(restTemplate.exchange(
                eq(TossPaymentConfig.PAYMENTS_URL + "/" + payKey + "/confirm"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(Void.class)
        )).thenReturn(new ResponseEntity<>(HttpStatus.OK));

        sut.markSuccess(payKey, orderId, amount);

        // 캡쳐해서 저장된 Payment 확인
        ArgumentCaptor<Payment> captor = ArgumentCaptor.forClass(Payment.class);
        verify(payRepo).save(captor.capture());
        Payment saved = captor.getValue();
        Assertions.assertThat(saved.getPaymentKey()).isEqualTo(payKey);
        Assertions.assertThat(saved.getPaymentStatus()).isEqualTo(PaymentStatus.SUCCESS);
        Assertions.assertThat(saved.getPayAmount()).isEqualTo(amount);
    }

    @Test
    @DisplayName("markFail: 실패 콜백 후 상태 업데이트")
    void markFail_ok() {
        String payKey = "p-fail";
        Payment existent = Payment.builder().paymentKey(payKey).build();
        when(payRepo.findByPaymentKey(payKey)).thenReturn(Optional.of(existent));

        sut.markFail(payKey, "err");

        Assertions.assertThat(existent.getPaymentStatus())
                .isEqualTo(PaymentStatus.FAIL);
        verify(payRepo).save(existent);
    }

    @Test
    @DisplayName("cancelPaymentPoint: 환불 API 호출 및 상태 업데이트")
    void cancelPaymentPoint_ok() {
        String payKey = "p-cancel";
        Payment existent = Payment.builder().paymentKey(payKey).build();
        when(payRepo.findByPaymentKey(payKey)).thenReturn(Optional.of(existent));

        Map<String,Object> cancelResp = Map.of("cancelled", true);
        when(restTemplate.exchange(
                eq(TossPaymentConfig.PAYMENTS_URL + "/" + payKey + "/cancel"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                ArgumentMatchers.<ParameterizedTypeReference<Map<String,Object>>>any()
        )).thenReturn(new ResponseEntity<>(cancelResp, HttpStatus.OK));

        Map<String,Object> result = sut.cancelPaymentPoint(payKey, "reason");

        // 반환값 확인 + 상태 저장 확인
        Assertions.assertThat(result).containsEntry("cancelled", true);
        Assertions.assertThat(existent.getPaymentStatus())
                .isEqualTo(PaymentStatus.CANCEL);
        verify(payRepo).save(existent);
    }
}