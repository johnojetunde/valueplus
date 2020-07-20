package com.codeemma.valueplus.domain.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Builder
@Getter
public class TransactionModel {
    private final Long id;
    private final String accountNumber;
    private final String bankCode;
    private final BigDecimal amount;
    private final String currency;
    private final String reference;
    private final String status;
    private final Long userId;
}
