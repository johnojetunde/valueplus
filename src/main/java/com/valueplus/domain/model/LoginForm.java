package com.valueplus.domain.model;

import lombok.Data;

import static java.util.Optional.ofNullable;

@Data
public class LoginForm {
    private String email;
    private String password;

    public String getEmail() {
        return ofNullable(email)
                .map(String::toLowerCase).orElse(email);
    }
}
