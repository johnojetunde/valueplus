package com.valueplus.domain.model;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

@Data
public class NewPassword {
    @NotEmpty
    private String resetToken;
    @NotEmpty
    @Size(min = 8, message = "minimum of 8 characters")
    private String newPassword;
}
