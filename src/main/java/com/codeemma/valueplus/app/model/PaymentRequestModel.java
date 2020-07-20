package com.codeemma.valueplus.app.model;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;

@Data
public class PaymentRequestModel {
    @NotBlank
    private String accountNumber;
    @NotBlank
    private String bankCode;
    @NotBlank
    private BigDecimal amount;
}
