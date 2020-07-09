package com.codeemma.valueplus.service;

import com.codeemma.valueplus.dto.RoleType;
import com.codeemma.valueplus.dto.UserCreate;
import com.codeemma.valueplus.dto.data4Me.AgentDto;
import com.codeemma.valueplus.model.Role;
import com.codeemma.valueplus.model.User;
import com.codeemma.valueplus.repository.RoleRepository;
import com.codeemma.valueplus.repository.UserRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class RegistrationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final Data4meService data4meService;

    public RegistrationService(UserRepository userRepository, @Qualifier("passwordEncoder") PasswordEncoder passwordEncoder,
                               RoleRepository roleRepository, Data4meService data4meService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
        this.data4meService = data4meService;
    }

    public User saveUser(UserCreate userCreate, RoleType roleType) {
        User user = userRepository.save(User.from(userCreate)
                .role(getRole(roleType))
                .password(passwordEncoder.encode(userCreate.getPassword())).build());

        data4meService.createAgent(AgentDto.from(userCreate)).ifPresent(v -> user.setAgentCode(v.getCode()));
        return user;
    }

    private Role getRole(RoleType roleType) {
        return roleRepository.findByName(roleType.name())
                .orElse(
                        roleRepository.save(Role.builder().name(roleType.name()).build())
                );

    }

}
