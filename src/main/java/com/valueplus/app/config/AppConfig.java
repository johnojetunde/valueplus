package com.valueplus.app.config;

import com.valueplus.paystack.model.PaystackConfig;
import org.apache.velocity.app.VelocityEngine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Clock;

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
                .domain(PaystackConfig.Domain.fromString(paystackDomain))
                .liveApiKey(paystackLiveApiKey)
                .testApiKey(paystackTestApiKey)
                .paymentReason("ValuePlus Payment")
                .transferCallBackUrl(transferCallbackUrl)
                .build();
    }

    @Bean
    public VelocityEngine velocityEngine() {
        VelocityEngine velocityEngine = new VelocityEngine();
        velocityEngine.setProperty("resource.loader", "class");
        velocityEngine.setProperty("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        return velocityEngine;
    }

    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }
}
