package com.codeemma.valueplus;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class ValueplusApplication {

    public static void main(String[] args) {
        SpringApplication.run(ValueplusApplication.class, args);
    }
}
