package com.nhnacademy.bookstoreorderapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.session.SessionAutoConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(
        exclude = { SessionAutoConfiguration.class }
)@EnableFeignClients(basePackages = "com.nhnacademy.bookstoreorderapi.order.client")
public class BookstoreOrderApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(BookstoreOrderApiApplication.class, args);
    }
}