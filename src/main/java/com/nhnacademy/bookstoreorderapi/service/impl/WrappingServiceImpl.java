package com.nhnacademy.bookstoreorderapi.service.impl;

import com.nhnacademy.bookstoreorderapi.entity.Wrapping;
import com.nhnacademy.bookstoreorderapi.repository.WrappingRepository;
import com.nhnacademy.bookstoreorderapi.service.WrappingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WrappingServiceImpl implements WrappingService {

    private final WrappingRepository wrappingRepository;

    @Override
    public int calculateFinalPrice(int totalPrice, Long wrappingId) {

        Optional<Wrapping> wrappingOptional = wrappingRepository.findById(wrappingId);

        int wrappingPrice = wrappingOptional.map(Wrapping::getPrice).orElse(0);

        return totalPrice + wrappingPrice;
    }
}
