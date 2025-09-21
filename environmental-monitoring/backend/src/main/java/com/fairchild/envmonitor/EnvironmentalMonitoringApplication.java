package com.fairchild.envmonitor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableAsync
@EnableCaching
public class EnvironmentalMonitoringApplication {

    public static void main(String[] args) {
        SpringApplication.run(EnvironmentalMonitoringApplication.class, args);
    }
}
