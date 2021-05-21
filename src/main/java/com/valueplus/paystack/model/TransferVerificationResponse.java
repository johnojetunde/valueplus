package com.valueplus.paystack.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = false)
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class TransferVerificationResponse {
    private Long id;
    private String reference;
    private Long integration;
    private String domain;
    private BigDecimal amount;
    private String currency;
    private String reason;
    private String transferCode;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Recipient recipient;
    private String source;
    private String status;
    private Object failures;
    private String titanCode;
    private LocalDateTime transferredAt;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Recipient {
        private String domain;
        private String type;
        private String currency;
        private String name;
        private String description;
        private String metadata;
        private String recipientCode;
        private boolean isActive;
        private String email;
        private Long id;
        private Long integration;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private TransferRecipient.Details details;
    }
}
