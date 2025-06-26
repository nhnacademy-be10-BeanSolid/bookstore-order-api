package com.nhnacademy.bookstoreorderapi.order.service.impl;

import com.nhnacademy.bookstoreorderapi.order.domain.entity.Wrapping;
import com.nhnacademy.bookstoreorderapi.order.dto.response.OrderResponse;
import com.nhnacademy.bookstoreorderapi.order.repository.WrappingRepository;
import com.nhnacademy.bookstoreorderapi.order.service.WrappingService;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

//@Service
//@Profile("!local")
@RequiredArgsConstructor
public class WrappingServiceImpl implements WrappingService {

    private final WrappingRepository wrappingRepository;

    @Override
    public List<OrderResponse> listByUser(String userId) {
        return List.of();
    }

    @Override
    public int calculateFinalPrice(int totalPrice, Long wrappingId) {

        Optional<Wrapping> wrappingOptional = wrappingRepository.findById(wrappingId);

        int wrappingPrice = wrappingOptional.map(Wrapping::getPrice).orElse(0);

        return totalPrice + wrappingPrice;
    }
}
