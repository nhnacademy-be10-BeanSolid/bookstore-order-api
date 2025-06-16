package com.nhnacademy.bookstoreorderapi.order.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
public class SchedulerConfig {

    @Bean
    public ThreadPoolTaskScheduler taskScheduler() {

        ThreadPoolTaskScheduler ts = new ThreadPoolTaskScheduler();
        ts.setPoolSize(5);
        ts.setThreadNamePrefix("dynamic-schedule-");
        ts.setRemoveOnCancelPolicy(true);

        return ts;
    }
}
