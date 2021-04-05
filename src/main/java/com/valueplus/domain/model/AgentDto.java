package com.valueplus.domain.model;

import com.valueplus.domain.enums.ProductProvider;
import com.valueplus.persistence.entity.User;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptySet;
import static java.util.Optional.ofNullable;

@NoArgsConstructor
@Getter
public class AgentDto extends UserDto {

    private static final String BASE_LINK = "https://play.google.com/store/apps/details?id=[Application_ID]&referrer=utm_campaign%3D";
    private String agentCode;
    private String link;
    private String photo;
    private boolean emailVerified;
    private String superAgentCode;
    private List<Map<ProductProvider, String>> referralData;

    @Builder
    public AgentDto(Long id,
                    String firstname,
                    String lastname,
                    String email,
                    String phone,
                    String address,
                    String roleType,
                    String agentCode,
                    String link,
                    String photo,
                    boolean emailVerified,
                    String referralCode,
                    String superAgentCode,
                    boolean enabled) {
        super(id, firstname, lastname, email, phone, address, roleType, referralCode, false, emptySet(), enabled);
        this.agentCode = agentCode;
        this.link = link;
        this.photo = photo;
        this.superAgentCode = superAgentCode;
        this.emailVerified = emailVerified;
        this.referralData = buildReferralData(referralCode, link);
    }

    public static AgentDto valueOf(User user) {
        return valueOf(user, null);
    }

    public static AgentDto valueOf(User user, String photo) {
        AgentDtoBuilder builder = builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstname(user.getFirstname())
                .lastname(user.getLastname())
                .phone(user.getPhone())
                .address(user.getAddress())
                .emailVerified(user.isEmailVerified())
                .roleType(user.getRole().getName())
                .referralCode(user.getReferralCode())
                .superAgentCode(ofNullable(user.getSuperAgent()).map(User::getReferralCode).orElse(null))
                .enabled(user.isEnabled())
                .photo(photo);

        ofNullable(user.getAgentCode())
                .ifPresent(agentCode -> builder.agentCode(agentCode).link(BASE_LINK.concat(agentCode)));

        var agentDto = builder.build();
        agentDto.setTransactionTokenSet(user.isTransactionTokenSet());
        agentDto.setAuthorities(extractAuthorities(user));
        return agentDto;
    }

    private List<Map<ProductProvider, String>> buildReferralData(String referralCode, String link) {
        List<Map<ProductProvider, String>> referral = new ArrayList<>();
        referral.add(Map.of(ProductProvider.DATA4ME, ofNullable(link).orElse("")));
        referral.add(Map.of(ProductProvider.VALUEPLUS, ofNullable(referralCode).orElse("")));

        return referral;
    }
}
