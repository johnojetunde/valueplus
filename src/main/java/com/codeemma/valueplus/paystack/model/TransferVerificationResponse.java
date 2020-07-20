package com.codeemma.valueplus.paystack.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TransferVerificationResponse extends TransferResponse {
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
