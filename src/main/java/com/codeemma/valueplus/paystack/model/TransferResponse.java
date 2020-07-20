package com.codeemma.valueplus.paystack.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TransferResponse {
    private Long id;
    private String reference;
    private Long integration;
    private String domain;
    private BigDecimal amount;
    private String currency;
    private String reason;
    private Long recipient;
    private String status;
    private String transferCode;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
