package com.codeemma.valueplus.paystack.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AccountNumberModel {
    private final String accountNumber;
    private final String accountName;
    private final Long bankId;
}
