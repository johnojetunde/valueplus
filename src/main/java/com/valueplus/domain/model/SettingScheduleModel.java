package com.valueplus.domain.model;

import com.valueplus.domain.enums.Status;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
public class SettingScheduleModel {
    private Long id;
    private BigDecimal commissionPercentage;
    private LocalDate effectiveDate;
    private Status status;
    private String initiator;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
