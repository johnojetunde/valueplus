package com.codeemma.valueplus.domain.dto;

import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
public class VerifyEmail {
    @NotEmpty
    private String verificationToken;
}
