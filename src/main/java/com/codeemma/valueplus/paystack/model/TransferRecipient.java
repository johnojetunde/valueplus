package com.codeemma.valueplus.paystack.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Builder
@Data
public class TransferRecipient {
    private Long id;
    private String name;
    private String recipientCode;
    private String type;
    private String currency;
    private String domain;
    private Long integration;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean isDeleted;
    private boolean active;
    private Details details;

    @Data
    static class Details {
        private String authorizationCode;
        private String accountNumber;
        private String accountName;
        private String bankCode;
        private String bankName;
    }
}
