package com.nhnacademy.bookstoreorderapi;

import com.nhnacademy.bookstoreorderapi.payment.config.TossPaymentConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableConfigurationProperties(TossPaymentConfig.class)
@EnableFeignClients(basePackages = "com.nhnacademy.bookstoreorderapi.order.client")
public class BookstoreOrderApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(BookstoreOrderApiApplication.class, args);
    }
}