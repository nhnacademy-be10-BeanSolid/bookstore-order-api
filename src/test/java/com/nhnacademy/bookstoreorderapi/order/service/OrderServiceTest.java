package com.nhnacademy.bookstoreorderapi.order.service;

import com.nhnacademy.bookstoreorderapi.order.domain.entity.Order;
import com.nhnacademy.bookstoreorderapi.order.dto.OrderItemDto;
import com.nhnacademy.bookstoreorderapi.order.dto.OrderRequestDto;
import com.nhnacademy.bookstoreorderapi.order.dto.OrderResponseDto;
import com.nhnacademy.bookstoreorderapi.order.repository.OrderRepository;
import com.nhnacademy.bookstoreorderapi.order.repository.WrappingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {
    @Mock
    private OrderRepository orderRepository;

    @Mock
    private WrappingRepository wrappingRepository;

    @InjectMocks
    private OrderService orderService;

    private OrderRequestDto baseGuestRequest;
    private OrderRequestDto baseMemberRequest;

    @BeforeEach
    void setUp() {
        baseGuestRequest = OrderRequestDto.builder()
                .orderType("guest")
                .guestName("테스트")
                .guestPhone("010-0000-0000")
                .deliveryDate(null)
                .items(Collections.singletonList(
                        OrderItemDto.builder()
                                .bookId(1L)
                                .quantity(2)
                                .giftWrapped(false)
                                .build()
                ))
                .build();

        baseMemberRequest = OrderRequestDto.builder()
                .orderType("member")
                .userId(100L)
                .deliveryDate(null)
                .items(Collections.singletonList(
                        OrderItemDto.builder()
                                .bookId(2L)
                                .quantity(1)
                                .giftWrapped(false)
                                .build()
                ))
                .build();
    }

    @Test
    void createOrder_WhenDeliveryDateNull_SetsTodayAsDeliveryAt() {
        // given
        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> {
                    Order o = invocation.getArgument(0);
                    o.setId(1L);
                    return o;
                });

        // when
        OrderResponseDto response = orderService.createOrder(baseGuestRequest);

        // then
        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(captor.capture());
        Order saved = captor.getValue();

        LocalDateTime expected = LocalDate.now().atStartOfDay();
        assertThat(saved.getDeliveryAt()).isEqualTo(expected);
        // 최종 응답에도 메시지에 잘 반영됐는지 확인
        assertThat(response.getMessage()).contains("총액").contains("배송비");
    }

    @Test
    void createOrder_WhenDeliveryDateSpecified_SetsSpecifiedDateAsDeliveryAt() {
        // given
        LocalDate custom = LocalDate.of(2025, 6, 15);
        baseMemberRequest.setDeliveryDate(custom);

        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> {
                    Order o = invocation.getArgument(0);
                    o.setId(2L);
                    return o;
                });

        // when
        OrderResponseDto response = orderService.createOrder(baseMemberRequest);

        // then
        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(captor.capture());
        Order saved = captor.getValue();

        LocalDateTime expected = custom.atStartOfDay();
        assertThat(saved.getDeliveryAt()).isEqualTo(expected);
    }
}