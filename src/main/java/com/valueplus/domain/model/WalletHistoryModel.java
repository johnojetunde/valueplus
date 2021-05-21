package com.valueplus.domain.model;

import com.valueplus.domain.enums.TransactionType;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class WalletHistoryModel {
    private final Long walletId;
    private final Long walletHistoryId;
    private final BigDecimal amount;
    private final TransactionType type;
    private final String description;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
}
