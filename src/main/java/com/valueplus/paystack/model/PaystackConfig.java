package com.valueplus.paystack.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Getter
@Builder
public class PaystackConfig {
    private final String baseUrl;
    private final Domain domain;
    private final String testApiKey;
    private final String liveApiKey;
    private final String transferCallBackUrl;
    private final String paymentReason;

    public enum Domain {
        TEST("test"), LIVE("live");

        private final String domain;

        Domain(String domain) {
            this.domain = domain;
        }

        public static Domain fromString(String domain) {
            for (Domain b : Domain.values()) {
                if (b.domain.equalsIgnoreCase(domain)) {
                    return b;
                }
            }
            return TEST;
        }
    }
}
