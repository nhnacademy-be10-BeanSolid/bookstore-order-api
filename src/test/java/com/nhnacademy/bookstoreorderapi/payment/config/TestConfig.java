package com.nhnacademy.bookstoreorderapi.payment.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.AuditorAware;

import java.util.Optional;

@TestConfiguration
public class TestConfig {

    @Bean
    @Primary
    public AuditorAware<String> auditorAware() {
        return () -> Optional.of("test-user");
    }
}