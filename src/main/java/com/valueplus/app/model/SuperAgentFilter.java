package com.valueplus.app.model;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.time.LocalDate;

@Data
public class SuperAgentFilter {
    @NotBlank
    private String superAgentCode;
    private LocalDate startDate;
    private LocalDate endDate;
}
