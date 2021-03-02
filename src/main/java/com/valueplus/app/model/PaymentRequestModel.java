package com.valueplus.app.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class PaymentRequestModel {
    @NotNull
    @Min(value = 1)
    private BigDecimal amount;
    @NotBlank
    @Size(min = 4, max = 4)
    private String pin;
}
