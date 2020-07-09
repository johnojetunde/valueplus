package com.codeemma.valueplus.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class UserCreate {
    @NotEmpty
    private String firstname;
    @NotEmpty
    private String lastname;
    @NotEmpty
    @Size(min = 8, message = "minimum of 8 characters")
    private String password;
    @NotEmpty
    @Email
    private String email;
    @NotEmpty
    private String phone;
    @NotEmpty
    private String address;
}
