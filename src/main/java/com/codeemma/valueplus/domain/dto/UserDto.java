package com.codeemma.valueplus.domain.dto;


import com.codeemma.valueplus.persistence.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class UserDto {

    private static final String BASE_LINK = "https://play.google.com/store/apps/details?id=[Application_ID]&referrer=utm_campaign%3D";
    private String firstname;
    private String lastname;
    private String email;
    private String phone;
    private String address;
    private String agentCode;
    private String link;
    private String photo;

    public static UserDto valueOf(User user) {
        return builder()
                .email(user.getEmail())
                .firstname(user.getFirstname())
                .lastname(user.getLastname())
                .phone(user.getPhone())
                .address(user.getAddress())
                .agentCode(user.getAgentCode())
                .link(BASE_LINK.concat(user.getAgentCode()))
                .build();
    }

    public static UserDto valueOf(User user, String photo) {
        return builder()
                .email(user.getEmail())
                .firstname(user.getFirstname())
                .lastname(user.getLastname())
                .phone(user.getPhone())
                .address(user.getAddress())
                .agentCode(user.getAgentCode())
                .link(BASE_LINK.concat(user.getAgentCode()))
                .photo(photo)
                .build();
    }
}