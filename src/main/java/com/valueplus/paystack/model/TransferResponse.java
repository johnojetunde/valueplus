package com.valueplus.paystack.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Builder
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
