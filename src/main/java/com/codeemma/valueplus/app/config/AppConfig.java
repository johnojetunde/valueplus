package com.codeemma.valueplus.app.config;

import com.codeemma.valueplus.paystack.model.PaystackConfig;
import com.codeemma.valueplus.paystack.model.PaystackConfig.Domain;
import org.apache.velocity.app.VelocityEngine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AppConfig {

    @Value("${paystack.base.url}")
    private String paystackUrl;

    @Value("${paystack.api.live.key}")
    private String paystackLiveApiKey;

    @Value("${paystack.api.test.key}")
    private String paystackTestApiKey;

    @Value("${paystack.api.domain:test}")
    private String paystackDomain;
    @Value("${paystack.api.transfer.callback}")
    private String transferCallbackUrl;

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }

    @Bean
    public PaystackConfig paystackConfig() {
        return PaystackConfig.builder()
                .baseUrl(paystackUrl)
                .domain(Domain.fromString(paystackDomain))
                .liveApiKey(paystackLiveApiKey)
                .testApiKey(paystackTestApiKey)
                .paymentReason("ValuePlus Payment")
                .transferCallBackUrl(transferCallbackUrl)
                .build();
    }

    @Bean
    public VelocityEngine velocityEngine() {
        return new VelocityEngine();
    }
}
