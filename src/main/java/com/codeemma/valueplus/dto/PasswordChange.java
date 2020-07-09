package com.codeemma.valueplus.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class PasswordChange {
    private String oldPassword;
    private String newPassword;
}
