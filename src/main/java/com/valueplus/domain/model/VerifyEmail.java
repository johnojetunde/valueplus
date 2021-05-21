package com.valueplus.domain.model;

import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
public class VerifyEmail {
    @NotEmpty
    private String verificationToken;
}
