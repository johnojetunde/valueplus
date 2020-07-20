package com.codeemma.valueplus.paystack.model;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Builder
@Getter
public class BankModel {
    private final Long id;
    private final String name;
    private final String slug;
    private final String cpde;
    private final String longcode;
    private final String gateway;
    private final boolean active;
    private final boolean isDeleted;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
}
