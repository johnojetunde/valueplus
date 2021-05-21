package com.valueplus.domain.model;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
@ToString
public class WalletModel {
    private final Long userId;
    private final Long walletId;
    private final BigDecimal amount;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
}
