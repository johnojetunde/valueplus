package com.codeemma.valueplus.domain.dto;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;

@Data
public class PasswordReset {
    @NotEmpty
    @Email
    private String email;
}
