package com.fairchild.envmonitor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class EnvironmentalMonitoringApplication {

    public static void main(String[] args) {
        SpringApplication.run(EnvironmentalMonitoringApplication.java, args);
    }
}
