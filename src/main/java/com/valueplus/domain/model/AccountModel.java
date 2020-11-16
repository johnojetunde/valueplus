package com.valueplus.domain.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AccountModel {
    private Long id;
    private Long userId;
    private String accountNumber;
    private String accountName;
    private String bankCode;
}
