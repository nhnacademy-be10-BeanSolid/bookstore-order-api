package com.nhnacademy.bookstoreorderapi.payment.service;

import com.nhnacademy.bookstoreorderapi.order.domain.entity.Order;
import com.nhnacademy.bookstoreorderapi.order.repository.OrderRepository;
import com.nhnacademy.bookstoreorderapi.payment.client.TossPaymentClient;
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
import org.springframework.http.ResponseEntity;

import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class PaymentServiceImplTest {

    OrderRepository   orderRepo;
    PaymentRepository payRepo;
    TossPaymentConfig tossProps;
    TossPaymentClient tossClient;     // ← RestTemplate 대신

    PaymentServiceImpl sut;

    @BeforeEach
    void setUp() {
        orderRepo  = mock(OrderRepository.class);
        payRepo    = mock(PaymentRepository.class);
        tossClient = mock(TossPaymentClient.class);
        tossProps  = new TossPaymentConfig();
        // 테스트용 dummy 설정
        tossProps.setSecretApiKey("dummy-sk");
        tossProps.setSuccessUrl("http://success");
        tossProps.setFailUrl("http://fail");

        sut = new PaymentServiceImpl(orderRepo, payRepo, tossProps, tossClient);
    }

    @Test
    @DisplayName("requestTossPayment: 정상 생성 플로우")
    void requestTossPayment_ok() {
        String orderId = "ORD-100";
        Order dummyOrder = mock(Order.class);

        // given
        when(orderRepo.findByOrderId(orderId)).thenReturn(dummyOrder);
        when(payRepo.findByOrder(dummyOrder)).thenReturn(Optional.empty());

        // Toss API 가 반환할 body
        Map<String,Object> fakeResp = Map.of(
                "paymentKey",  "payKey-abc",
                "checkoutUrl", "https://toss.test"
        );
        // Feign client 모킹
        when(tossClient.createPayment(anyString(), anyMap()))
                .thenReturn(ResponseEntity.ok(fakeResp));

        PaymentReqDto req = PaymentReqDto.builder()
                .payAmount(10_000L)
                .payType(PayType.CARD)
                .payName("테스트 결제")
                .build();

        PaymentResDto res = sut.requestTossPayment(orderId, req);

        Assertions.assertThat(res.getPaymentKey()).isEqualTo("payKey-abc");
        Assertions.assertThat(res.getRedirectUrl()).isEqualTo("https://toss.test");

        verify(orderRepo).findByOrderId(orderId);
        verify(payRepo).findByOrder(dummyOrder);
        verify(tossClient).createPayment(anyString(), anyMap());
    }

    @Test
    @DisplayName("markSuccess: 성공 콜백 후 저장로직 호출")
    void markSuccess_ok() {
        String orderId = "O-1";
        String payKey  = "p-1";
        long   amount  = 5_000L;

        Order o = mock(Order.class);
        when(orderRepo.findByOrderId(orderId)).thenReturn(o);
        when(payRepo.findByOrder(o)).thenReturn(Optional.empty());

        // confirmPayment 는 void 반환이지만 FeignClient signature 가 ResponseEntity<Void>
        when(tossClient.confirmPayment(anyString(), eq(payKey), anyMap()))
                .thenReturn(ResponseEntity.ok().build());

        // when
        sut.markSuccess(payKey, orderId, amount);

        // then: 저장된 Payment 캡쳐
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

        sut.markFail(payKey, "error-msg");

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
        when(tossClient.cancelPayment(anyString(), eq(payKey), anyMap()))
                .thenReturn(ResponseEntity.ok(cancelResp));

        Map<String,Object> result = sut.cancelPaymentPoint(payKey, "사유");

        Assertions.assertThat(result).containsEntry("cancelled", true);
        Assertions.assertThat(existent.getPaymentStatus())
                .isEqualTo(PaymentStatus.CANCEL);
        verify(payRepo).save(existent);
    }
}