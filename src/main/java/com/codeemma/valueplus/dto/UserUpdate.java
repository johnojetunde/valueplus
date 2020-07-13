package com.codeemma.valueplus.dto;

import com.codeemma.valueplus.model.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
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
