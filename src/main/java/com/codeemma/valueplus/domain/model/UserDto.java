package com.codeemma.valueplus.domain.model;


import com.codeemma.valueplus.persistence.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static java.util.Optional.ofNullable;

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
    private boolean emailVerified;

    public static UserDto valueOf(User user) {
        return valueOf(user, null);
    }

    public static UserDto valueOf(User user, String photo) {
        UserDtoBuilder builder = builder()
                .email(user.getEmail())
                .firstname(user.getFirstname())
                .lastname(user.getLastname())
                .phone(user.getPhone())
                .address(user.getAddress())
                .emailVerified(user.isEmailVerified())
                .photo(photo);

        ofNullable(user.getAgentCode())
                .ifPresent(agentCode -> builder.agentCode(agentCode).link(BASE_LINK.concat(agentCode)));

        return builder.build();
    }
}
