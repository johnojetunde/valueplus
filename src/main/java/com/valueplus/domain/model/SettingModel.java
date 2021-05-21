package com.valueplus.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
public class SettingModel {
    @NotNull
    @Min(1)
    private BigDecimal commissionPercentage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
