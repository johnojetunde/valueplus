package com.codeemma.valueplus.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class LoginToken {
    private String token;
    private boolean passwordReset;
}
