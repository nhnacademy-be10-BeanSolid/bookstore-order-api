package com.nhnacademy.bookstoreorderapi;

import com.nhnacademy.bookstoreorderapi.payment.config.TossPaymentConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(TossPaymentConfig.class)
public class BookstoreOrderApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(BookstoreOrderApiApplication.class, args);
    }
}