package com.codeemma.valueplus.domain.model;

import com.codeemma.valueplus.persistence.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {
    private String firstname;
    private String lastname;
    private String email;
    private String phone;
    private String address;
    private String roleType;

    public static UserDto valueOf(User user) {
        return new UserDto(
                user.getEmail(),
                user.getFirstname(),
                user.getLastname(),
                user.getPhone(),
                user.getAddress(),
                user.getRole().getName());
    }
}
