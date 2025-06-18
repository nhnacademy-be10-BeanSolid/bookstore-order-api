package com.nhnacademy.bookstoreorderapi;

import com.nhnacademy.bookstoreorderapi.payment.config.TossPaymentConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EntityScan(basePackages = "com.nhnacademy.bookstoreorderapi")   // 내 패키지만 스캔

public class BookstoreOrderApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(BookstoreOrderApiApplication.class, args);
    }
}