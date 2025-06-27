//package com.nhnacademy.bookstoreorderapi.payment.integration;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.nhnacademy.bookstoreorderapi.order.domain.entity.Order;
//import com.nhnacademy.bookstoreorderapi.order.domain.entity.OrderStatus;
//import com.nhnacademy.bookstoreorderapi.order.domain.entity.ShippingInfo;
//import com.nhnacademy.bookstoreorderapi.order.repository.OrderRepository;
//import com.nhnacademy.bookstoreorderapi.payment.config.TossPaymentConfig;
//import com.nhnacademy.bookstoreorderapi.payment.domain.PayType;
//import com.nhnacademy.bookstoreorderapi.payment.dto.Request.PaymentReqDto;
//import com.nhnacademy.bookstoreorderapi.payment.dto.Response.PaymentResDto;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.http.HttpMethod;
//import org.springframework.http.MediaType;
//import org.springframework.test.context.ActiveProfiles;
//import org.springframework.test.web.client.MockRestServiceServer;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.web.client.RestTemplate;
//
//import java.time.LocalDate;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.springframework.test.web.client.ExpectedCount.once;
//import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
//import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//@ActiveProfiles("test")
//@AutoConfigureMockMvc
//@AutoConfigureTestDatabase  // H2 로 자동 교체
//public class PaymentIntegrationTest {
//
//    @Autowired MockMvc mvc;
//    @Autowired ObjectMapper om;
//    @Autowired OrderRepository orderRepository;
//    @Autowired RestTemplate restTemplate;
//
//    MockRestServiceServer mockServer;
//
//    @BeforeEach
//    void setUp() {
//        // 1) DB 초기화 및 주문 생성
//        orderRepository.deleteAll();
//
//        ShippingInfo shipping = new ShippingInfo(
//                LocalDate.now().plusDays(3),
//                "서울 강남구 테헤란로 123",
//                "홍길동",
//                "010-1234-5678",
//                2500
//        );
//
//        Order order = Order.builder()
//                .orderId("ORD-123")
//                .userId(1L)
//                .orderDate(LocalDate.now())
//                .totalPrice(15000)
//                .status(OrderStatus.PENDING)
//                .shippingInfo(shipping)
//                .build();
//
//        orderRepository.save(order);
//
//        // 2) RestTemplate 호출 가로채기 (Toss API Mock)
//        mockServer = MockRestServiceServer.createServer(restTemplate);
//        mockServer.expect(once(), requestTo(TossPaymentConfig.PAYMENTS_URL))
//                .andExpect(method(HttpMethod.POST))
//                .andRespond(withSuccess(
//                        "{\"paymentKey\":\"payKey-abc\",\"checkoutUrl\":\"https://redirect.test\"}",
//                        MediaType.APPLICATION_JSON
//                ));
//    }
//
//    @Test
//    void fullPaymentFlow() throws Exception {
//        // 결제 요청 DTO
//        PaymentReqDto req = PaymentReqDto.builder()
//                .payAmount(5000L)
//                .payType(PayType.CARD)
//                .payName("통합 결제 테스트")
//                .build();
//
//        // 1) /toss/{orderId} 호출 → 201 응답
//        String json = mvc.perform(post("/api/v1/payments/toss/{orderId}", "ORD-123")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(om.writeValueAsString(req)))
//                .andExpect(status().isCreated())
//                .andReturn()
//                .getResponse()
//                .getContentAsString();
//
//        // 2) 응답 바디에서 orderId 검증
//        PaymentResDto res = om.readValue(json, PaymentResDto.class);
//        assertEquals("ORD-123", res.getOrderId());
//
//        mockServer.verify();  // RestTemplate mocking 호출 검증
//    }
//}