package com.codeemma.valueplus.domain.dto;

import com.codeemma.valueplus.domain.enums.TransactionStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
@Getter
public class TransactionModel {
    private final Long id;
    private final String accountNumber;
    private final String bankCode;
    private final BigDecimal amount;
    private final String currency;
    private final String reference;
    private final TransactionStatus status;
    private final Long userId;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
}
