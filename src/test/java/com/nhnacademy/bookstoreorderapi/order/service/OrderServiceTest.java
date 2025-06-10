package com.nhnacademy.bookstoreorderapi.order.service;

import com.nhnacademy.bookstoreorderapi.order.domain.entity.Order;
import com.nhnacademy.bookstoreorderapi.order.domain.entity.Wrapping;
import com.nhnacademy.bookstoreorderapi.order.domain.entity.OrderStatus;
import com.nhnacademy.bookstoreorderapi.order.dto.OrderItemDto;
import com.nhnacademy.bookstoreorderapi.order.dto.OrderRequestDto;
import com.nhnacademy.bookstoreorderapi.order.dto.OrderResponseDto;
import com.nhnacademy.bookstoreorderapi.order.repository.OrderRepository;
import com.nhnacademy.bookstoreorderapi.order.repository.WrappingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private WrappingRepository wrappingRepository;

    @InjectMocks
    private OrderService orderService;

    private Wrapping sampleWrapping;

    @BeforeEach
    void setUp() {
        // 샘플 wrapping 엔티티 설정
        sampleWrapping = Wrapping.builder()
                .id(1L)
                .name("기본 포장")
                .price(2000)
                .active(true)
                .build();
    }

    @Test
    @DisplayName("createOrder - 게스트 주문 시 wrappingId 없는 경우 정상 계산")
    void createGuestOrder_NoWrapping() {
        OrderItemDto item = OrderItemDto.builder()
                .bookId(100L)
                .quantity(2)
                .giftWrapped(false)
                .wrappingId(null)
                .build();
        OrderRequestDto req = OrderRequestDto.builder()
                .orderType("guest")
                .guestName("홍길동")
                .guestPhone("010-1111-2222")
                .deliveryDate(LocalDate.of(2025, 6, 20))
                .items(List.of(item))
                .build();

        // orderRepository.save가 전달받은 Order에 id 설정 후 반환
        given(orderRepository.save(any(Order.class))).willAnswer(invocation -> {
            Order o = invocation.getArgument(0);
            o.setId(1L);
            return o;
        });

        OrderResponseDto resp = orderService.createOrder(req);

        assertEquals(1L, resp.getOrderId());
        // 단가 10_000 * 2 = 20000 + wrap 0
        assertEquals(20000, resp.getTotalPrice());
        assertEquals(3000, resp.getDeliveryFee());
        assertEquals(23000, resp.getFinalPrice());
        assertTrue(resp.getMessage().contains("홍길동"));
    }

    @Test
    @DisplayName("createOrder - 게스트 주문 시 wrappingId 있는 경우 정상 계산")
    void createGuestOrder_WithWrapping() {
        OrderItemDto item = OrderItemDto.builder()
                .bookId(200L)
                .quantity(3)
                .giftWrapped(true)
                .wrappingId(1L)
                .build();
        OrderRequestDto req = OrderRequestDto.builder()
                .orderType("guest")
                .guestName("테스트")
                .guestPhone("010-3333-4444")
                .deliveryDate(LocalDate.of(2025, 6, 21))
                .items(List.of(item))
                .build();

        given(wrappingRepository.findById(1L)).willReturn(Optional.of(sampleWrapping));
        given(orderRepository.save(any(Order.class))).willAnswer(invocation -> {
            Order o = invocation.getArgument(0);
            o.setId(2L);
            return o;
        });

        OrderResponseDto resp = orderService.createOrder(req);


        // unit 10000*3 + wrap 2000*3 = 30000 + 6000 = 36000
        assertEquals(36000, resp.getTotalPrice());
        assertEquals(3000, resp.getDeliveryFee());
        assertEquals(39000, resp.getFinalPrice());
        assertEquals(2L, resp.getOrderId());
        assertTrue(resp.getMessage().contains("테스트"));
    }

    @Test
    @DisplayName("createOrder - 유효하지 않은 wrappingId 일 때 예외 발생")
    void createOrder_InvalidWrappingId() {
        // given
        OrderItemDto item = OrderItemDto.builder()
                .bookId(300L)
                .quantity(1)
                .giftWrapped(true)
                .wrappingId(99L)
                .build();
        OrderRequestDto req = OrderRequestDto.builder()
                .orderType("guest")
                .guestName("테스트2")
                .guestPhone("010-5555-6666")
                .deliveryDate(LocalDate.of(2025, 6, 22))
                .items(List.of(item))
                .build();

        given(wrappingRepository.findById(99L)).willReturn(Optional.empty());

        // when & then
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            orderService.createOrder(req);
        });
        assertTrue(ex.getMessage().contains("유효하지 않은 포장 ID: 99"));
    }

    @Test
    @DisplayName("listAll - 모든 주문을 OrderResponseDto로 변환하여 반환")
    void listAll_ReturnsDtoList() {
        // given
        Order o1 = Order.builder()
                .id(10L)
                .userId(50L)
                .guestName(null)
                .guestPhone(null)
                .status(OrderStatus.PENDING)
                .requestedAt(LocalDateTime.now())
                .deliveryAt(LocalDateTime.now().plusDays(1))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .totalPrice(10000)
                .deliveryFee(3000)
                .finalPrice(13000)
                .build();
        Order o2 = Order.builder()
                .id(11L)
                .userId(null)
                .guestName("손님")
                .guestPhone("010-7777-8888")
                .status(OrderStatus.PENDING)
                .requestedAt(LocalDateTime.now())
                .deliveryAt(LocalDateTime.now().plusDays(2))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .totalPrice(20000)
                .deliveryFee(3000)
                .finalPrice(23000)
                .build();
        given(orderRepository.findAll()).willReturn(List.of(o1, o2));

        // when
        List<OrderResponseDto> list = orderService.listAll();

        // then
        assertEquals(2, list.size());

        OrderResponseDto dto1 = list.get(0);
        assertEquals(10L, dto1.getOrderId());
        assertEquals(10000, dto1.getTotalPrice());

        OrderResponseDto dto2 = list.get(1);
        assertEquals(11L, dto2.getOrderId());
        assertEquals(20000, dto2.getTotalPrice());
    }
}