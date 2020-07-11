package com.codeemma.valueplus.dto;

import com.codeemma.valueplus.model.User;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;

public class UserUpdate {
    @NotEmpty
    private String firstname;
    @NotEmpty
    private String lastname;
    @NotEmpty
    private String phone;
    @NotEmpty
    private String address;

    public User toUser(Long id) {
        return User.builder()
                .id(id)
                .firstname(firstname)
                .lastname(lastname)
                .phone(phone)
                .address(address)
                .build();
    }
}
