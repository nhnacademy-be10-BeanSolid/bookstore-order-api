//package com.nhnacademy.bookstoreorderapi.order.service.impl;
//
//import com.nhnacademy.bookstoreorderapi.order.domain.entity.Wrapping;
//import com.nhnacademy.bookstoreorderapi.order.repository.WrappingRepository;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import java.util.Optional;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.mockito.Mockito.when;
//
//@ExtendWith(MockitoExtension.class)
//class WrappingServiceImplTest {
//
//    @Mock
//    private WrappingRepository wrappingRepository;
//
//    @InjectMocks
//    private WrappingServiceImpl wrappingService;
//
//
//    @Test
//    @DisplayName("포장 옵션 선택 시 총 금액에 포장지 금액 추가")
//    void calculateTotalPrice_withWrapping() {
//
//        long wrappingId = 1L;
//        int totalPrice = 10_000;
//        Wrapping wrapping = new Wrapping(wrappingId, "포장지1", 5_000, true);
//        when(wrappingRepository.findById(wrappingId))
//                .thenReturn(Optional.of(wrapping));
//
//        int finalPrice = wrappingService.calculateFinalPrice(totalPrice, wrappingId);
//
//        assertThat(finalPrice).isEqualTo(15000);
//    }
//
//    @Test
//    @DisplayName("포장 옵션 미선택 시 총 금액은 그대로 유지")
//    void calculateTotalPrice_NoWrapping() {
//
//        int totalPrice = 20_000;
//        int finalPrice = wrappingService.calculateFinalPrice(totalPrice, null);
//
//        assertThat(finalPrice).isEqualTo(20_000);
//    }
//}