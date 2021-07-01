package com.valueplus.domain.model;

import com.valueplus.domain.enums.ProductProvider;
import com.valueplus.domain.products.ProductProviderUrlService;
import com.valueplus.persistence.entity.User;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

import static com.valueplus.domain.util.FunctionUtil.emptyIfNullStream;
import static java.util.Collections.emptySet;
import static java.util.Optional.ofNullable;

@NoArgsConstructor
@Getter
public class AgentDto extends UserDto {

    private static final String BASE_LINK = "https://play.google.com/store/apps/details?id=je.data4me.jara&referrer=utm_campaign%3D";
    private String agentCode;
    private String link;
    private String photo;
    private boolean emailVerified;
    private String superAgentCode;
    @Setter
    private Map<ProductProvider, String> referralData;

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
                    boolean enabled,
                    Map<ProductProvider, String> referralData) {
        super(id, firstname, lastname, email, phone, address, roleType, referralCode, false, emptySet(), enabled);
        this.agentCode = agentCode;
        this.link = link;
        this.photo = photo;
        this.superAgentCode = superAgentCode;
        this.emailVerified = emailVerified;
        this.referralData = referralData;
    }

    public static AgentDto valueOf(User user) {
        return valueOf(user, null, new HashMap<>());
    }

    public static AgentDto valueOf(User user, Map<ProductProvider, ProductProviderUrlService> providerUrlServiceMap) {
        return valueOf(user, null, providerUrlServiceMap);
    }

    public static AgentDto valueOf(User user, String photo, Map<ProductProvider, ProductProviderUrlService> providerUrlServiceMap) {
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

        Map<ProductProvider, String> referralData = new HashMap<>();

        if (!providerUrlServiceMap.isEmpty()) {
            emptyIfNullStream(user.getProductProviders())
                    .forEach(s -> {
                        String agentUrl = providerUrlServiceMap.get(s.getProvider()).getReferralUrl(s.getAgentUrl());
                        referralData.put(s.getProvider(), agentUrl);
                    });
        }


        var agentDto = builder.build();
        agentDto.setTransactionTokenSet(user.isTransactionTokenSet());
        agentDto.setAuthorities(extractAuthorities(user));
        agentDto.setReferralData(referralData);
        return agentDto;
    }
}
