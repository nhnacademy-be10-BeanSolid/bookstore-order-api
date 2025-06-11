package com.nhnacademy.bookstoreorderapi.order.service;

import com.nhnacademy.bookstoreorderapi.order.domain.entity.Order;
import com.nhnacademy.bookstoreorderapi.order.domain.entity.OrderStatus;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
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

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

    @ParameterizedTest(name = "주문#{0} 상태를 {1}에서 {2}로 변경하면 성공한다")
    @CsvSource({
            "1, PENDING, SHIPPING",
            "2, PENDING, CANCELED",
            "3, SHIPPING, COMPLETED",
            "4, SHIPPING, CANCELED",
            "5, COMPLETED, RETURNED"
    })
    void changeStatus_fromPending_Success(long orderId, OrderStatus oldStatus, OrderStatus newStatus) {

        Order order = new Order();
        order.setId(orderId);
        order.setStatus(oldStatus);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        orderService.changeStatus(orderId, newStatus);

        assertThat(order.getStatus())
                .as("주문상태 변환 성공")
                .isEqualTo(newStatus);
        verify(orderRepository, times(1)).findById(orderId);
    }

    @ParameterizedTest(name = "주문#{0} 상태를 {1}에서 {2}로 변경하면 실패한다")
    @CsvSource({
            "1, PENDING, COMPLETED",
            "2, PENDING, RETURNED",
            "3, SHIPPING, PENDING",
            "4, SHIPPING, RETURNED",
            "5, COMPLETED, PENDING",
            "6, COMPLETED, SHIPPING",
            "7, COMPLETED, CANCELED",
            "8, RETURNED, PENDING",
            "9, RETURNED, SHIPPING",
            "10, RETURNED, COMPLETED",
            "11, RETURNED, CANCELED",
            "12, CANCELED, PENDING",
            "13, CANCELED, SHIPPING",
            "14, CANCELED, COMPLETED",
            "15, CANCELED, RETURNED"
    })
    void changeStatus_fromPending_Failed(long orderId, OrderStatus oldStatus, OrderStatus newStatus) {

        Order order = new Order();
        order.setId(orderId);
        order.setStatus(oldStatus);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.changeStatus(orderId, newStatus))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("상태 전이 불가");
        verify(orderRepository, times(1)).findById(orderId);
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

