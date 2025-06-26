package com.nhnacademy.bookstoreorderapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication

// ConfigurationProperties 달린 설정 객체들을 패키지 전체에서 찾아서 빈 등록까지 알아서 해줌
@ConfigurationPropertiesScan
@EnableFeignClients(basePackages = "com.nhnacademy.bookstoreorderapi.order.client")
public class BookstoreOrderApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(BookstoreOrderApiApplication.class, args);
    }
}