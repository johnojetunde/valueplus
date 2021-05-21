package com.valueplus.domain.model;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class SettingLogModel {
    private Long id;
    private BigDecimal commissionPercentage;
    private String initiator;
    private BigDecimal prevCommissionPercentage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
