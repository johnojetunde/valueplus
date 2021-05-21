package com.valueplus.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class AccountSummary {
    Integer totalAgents;
    BigDecimal totalProductSales;
    BigDecimal totalProductAgentProfits;
    BigDecimal totalApprovedWithdrawals;
    BigDecimal totalPendingWithdrawal;
    Integer pendingWithdrawalCount;
    Integer totalActiveUsers;
    Integer totalDownloads;
}
