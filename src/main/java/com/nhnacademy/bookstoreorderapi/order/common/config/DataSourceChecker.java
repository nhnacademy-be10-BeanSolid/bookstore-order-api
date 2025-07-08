package com.nhnacademy.bookstoreorderapi.order.common.config;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

@Component
@RequiredArgsConstructor
public class DataSourceChecker implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataSourceChecker.class);

    private final DataSource dataSource;

    @Override
    public void run(String... args) {
        logger.info("DATASOURCE = {}", dataSource.getClass().getName());
    }
}

