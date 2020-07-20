package com.codeemma.valueplus.app.config;

import com.codeemma.valueplus.paystack.model.PaystackConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AppConfig {

    @Value("${paystack.base.url}")
    private String paystackUrl;

    @Value("${paystack.api.key}")
    private String paystackApiKey;

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }

    @Bean
    public PaystackConfig paystackConfig() {
        return new PaystackConfig(paystackUrl, paystackApiKey);
    }
}
