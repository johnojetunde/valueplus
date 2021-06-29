package com.valueplus.domain.model;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;

import static java.util.Optional.ofNullable;

@Data
public class PasswordReset {
    @NotEmpty
    @Email
    private String email;

    public String getEmail() {
        return ofNullable(email)
                .map(String::toLowerCase).orElse(email);
    }
}
