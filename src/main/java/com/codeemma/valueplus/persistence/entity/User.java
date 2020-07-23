package com.codeemma.valueplus.persistence.entity;

import com.codeemma.valueplus.domain.dto.UserCreate;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Entity
@Table(name = "users")
public class User extends BasePersistentEntity implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String firstname;
    @Column(nullable = false)
    private String lastname;
    @Column(nullable = false)
    private String password;
    @Column(nullable = false, unique = true)
    private String email;
    @Column(nullable = false)
    private String phone;
    @Column(nullable = false)
    private String address;
    @Setter
    private String agentCode;
    private boolean passwordReset;
    private boolean emailVerified;

    @OneToOne
    @JoinColumn(name = "role_id")
    private Role role;

    private boolean enabled = true;
    private boolean deleted = false;

    public static UserBuilder from(UserCreate userCreate) {
        return builder().email(userCreate.getEmail())
                .firstname(userCreate.getFirstname())
                .lastname(userCreate.getLastname())
                .phone(userCreate.getPhone())
                .address(userCreate.getAddress());
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorities = new ArrayList<>();
        Role role = getRole();
        if (role != null) {
            String roleName = "ROLE_" + role.getName();
            authorities.add(new SimpleGrantedAuthority(roleName));
        }
        return authorities;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    public UserBuilder toBuilder() {
        return builder()
                .id(id)
                .firstname(firstname)
                .lastname(lastname)
                .email(email)
                .password(password)
                .phone(phone)
                .address(address)
                .agentCode(agentCode)
                .enabled(enabled)
                .deleted(deleted)
                .role(role);
    }
}
