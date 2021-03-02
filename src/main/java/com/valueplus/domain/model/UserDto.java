package com.valueplus.domain.model;

import com.valueplus.persistence.entity.User;
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
    private boolean isTransactionTokenSet;


    public static UserDto valueOf(User user) {
        return new UserDto(
                user.getFirstname(),
                user.getLastname(),
                user.getEmail(),
                user.getPhone(),
                user.getAddress(),
                user.getRole().getName(),
                user.isTransactionTokenSet()
        );
    }
}
