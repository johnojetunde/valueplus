package com.codeemma.valueplus.domain.model;

import com.codeemma.valueplus.persistence.entity.User;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static java.util.Optional.ofNullable;

@NoArgsConstructor
@Getter
public class AgentDto extends UserDto {

    private static final String BASE_LINK = "https://play.google.com/store/apps/details?id=[Application_ID]&referrer=utm_campaign%3D";
    private String agentCode;
    private String link;
    private String photo;
    private boolean emailVerified;

    @Builder
    public AgentDto(String firstname,
                    String lastname,
                    String email,
                    String phone,
                    String address,
                    String roleType,
                    String agentCode,
                    String link,
                    String photo,
                    boolean emailVerified) {
        super(firstname, lastname, email, phone, address, roleType);
        this.agentCode = agentCode;
        this.link = link;
        this.photo = photo;
        this.emailVerified = emailVerified;
    }

    public static AgentDto valueOf(User user) {
        return valueOf(user, null);
    }

    public static AgentDto valueOf(User user, String photo) {
        AgentDtoBuilder builder = builder()
                .email(user.getEmail())
                .firstname(user.getFirstname())
                .lastname(user.getLastname())
                .phone(user.getPhone())
                .address(user.getAddress())
                .emailVerified(user.isEmailVerified())
                .roleType(user.getRole().getName())
                .photo(photo);

        ofNullable(user.getAgentCode())
                .ifPresent(agentCode -> builder.agentCode(agentCode).link(BASE_LINK.concat(agentCode)));

        return builder.build();
    }
}