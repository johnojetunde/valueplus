package com.codeemma.valueplus.domain.dto;

import lombok.Data;

import javax.validation.constraints.Email;

@Data
public class PasswordReset {
    @Email
    private String email;
}
