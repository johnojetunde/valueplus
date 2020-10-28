package com.codeemma.valueplus.persistence.entity;

import com.codeemma.valueplus.domain.model.AgentCreate;
import com.codeemma.valueplus.domain.model.UserCreate;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
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
    private String phone;
    private String address;
    @Setter
    private String agentCode;
    private boolean emailVerified;

    @OneToOne
    @JoinColumn(name = "role_id")
    private Role role;

    private boolean enabled = true;
    private boolean deleted = false;

    public static UserBuilder from(AgentCreate agentCreate) {
        return builder()
                .email(agentCreate.getEmail().toLowerCase())
                .firstname(agentCreate.getFirstname())
                .lastname(agentCreate.getLastname())
                .phone(agentCreate.getPhone())
                .address(agentCreate.getAddress());
    }

    public static UserBuilder from(UserCreate agentCreate) {
        return builder()
                .email(agentCreate.getEmail().toLowerCase())
                .firstname(agentCreate.getFirstname())
                .lastname(agentCreate.getLastname())
                .phone(agentCreate.getPhone());
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
                .emailVerified(emailVerified)
                .enabled(enabled)
                .deleted(deleted)
                .role(role);
    }
}
