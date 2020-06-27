package com.codeemma.valueplus.dto;


import com.codeemma.valueplus.model.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class UserDto {
    private String firstname;
    private String lastname;
    private String email;
    private String agentCode;

    public static UserDto valueOf(User user) {
        return builder()
                .email(user.getEmail())
                .firstname(user.getFirstname())
                .lastname(user.getLastname())
                .agentCode(user.getAgentCode())
                .build();
    }
}
