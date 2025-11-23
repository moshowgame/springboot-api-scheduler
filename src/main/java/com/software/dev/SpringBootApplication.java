package com.software.dev;

import org.springframework.boot.SpringApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@org.springframework.boot.autoconfigure.SpringBootApplication
@EnableScheduling
public class SpringBootApplication {
    public static void main(String[] args) {
        SpringApplication.run(SpringBootApplication.class, args);
    }
}