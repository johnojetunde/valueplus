package com.valueplus;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.annotation.PostConstruct;
import java.util.TimeZone;

import static java.util.TimeZone.setDefault;

@EnableScheduling
@SpringBootApplication
public class ValueplusApplication {

    public static void main(String[] args) {
        SpringApplication.run(ValueplusApplication.class, args);
    }

    @PostConstruct
    public void init(){
        setDefault(TimeZone.getTimeZone("Africa/Lagos"));
    }
}
