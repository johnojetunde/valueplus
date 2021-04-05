package com.valueplus.domain.model;

import com.valueplus.persistence.entity.Authority;
import com.valueplus.persistence.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

import static com.valueplus.domain.util.FunctionUtil.emptyIfNullStream;
import static java.util.stream.Collectors.toSet;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {
    private Long id;
    private String firstname;
    private String lastname;
    private String email;
    private String phone;
    private String address;
    private String roleType;
    private String referralCode;
    private boolean isTransactionTokenSet;
    private Set<String> authorities;
    private boolean enabled;


    public static UserDto valueOf(User user) {
        return new UserDto(
                user.getId(),
                user.getFirstname(),
                user.getLastname(),
                user.getEmail(),
                user.getPhone(),
                user.getAddress(),
                user.getRole().getName(),
                user.getReferralCode(),
                user.isTransactionTokenSet(),
                extractAuthorities(user),
                user.isEnabled()
        );
    }

    static Set<String> extractAuthorities(User user) {
        return emptyIfNullStream(user.getUserAuthorities())
                .map(Authority::getAuthority)
                .collect(toSet());
    }
}
