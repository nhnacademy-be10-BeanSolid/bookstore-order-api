package com.nhnacademy.bookstoreorderapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.session.SessionAutoConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;

// JPA/DataSource 자동구성 제외
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;

@SpringBootApplication(
        exclude = {
                SessionAutoConfiguration.class,
                DataSourceAutoConfiguration.class,
                HibernateJpaAutoConfiguration.class,
                DataSourceTransactionManagerAutoConfiguration.class
        }
)
@EnableFeignClients(basePackages = "com.nhnacademy.bookstoreorderapi.order.client")
public class BookstoreOrderApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(BookstoreOrderApiApplication.class, args);
    }
}