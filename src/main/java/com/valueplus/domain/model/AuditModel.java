package com.valueplus.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.valueplus.domain.enums.ActionType;
import com.valueplus.domain.enums.EntityType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
@AllArgsConstructor
@Data
public class AuditModel {
    @NotNull(message = "Entity type is required")
    private EntityType entityType;
    private LocalDate startDate;
    private LocalDate endDate;
    private ActionType action;
}
