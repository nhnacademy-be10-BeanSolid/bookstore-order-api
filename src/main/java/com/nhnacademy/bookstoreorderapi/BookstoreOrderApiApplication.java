package com.nhnacademy.bookstoreorderapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication

//TossPaymentConfig를 찾아서 yml에 정의된 프로퍼티 값을 자동으로 매핑해줌.
@ConfigurationPropertiesScan

//client같은 패키지 안에 정의한 애플리케이션 컨텍스트에 자동으로 등록해줌
@EnableFeignClients(basePackages = {
        "com.nhnacademy.bookstoreorderapi.order.client",
        "com.nhnacademy.bookstoreorderapi.payment.client"
})
@EnableJpaAuditing
public class BookstoreOrderApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(BookstoreOrderApiApplication.class, args);
    }
}