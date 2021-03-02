package com.valueplus.domain.model;

import lombok.*;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class SettingModel {
    @NotNull
    @Min(1)
    private BigDecimal commissionPercentage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
