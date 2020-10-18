package com.codeemma.valueplus.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class AccountSummary {
    BigDecimal totalWalletAmount;
    BigDecimal totalProductSales;
    BigDecimal totalProductAgentProfits;
    BigDecimal totalApprovedWithdrawals;
    BigDecimal totalPendingWithdrawal;
    Integer pendingWithdrawalCount;
    Integer activeUser;
    Integer totalDownloads;
}
