package com.codeemma.valueplus.domain.service;

import com.codeemma.valueplus.domain.dto.RoleType;
import com.codeemma.valueplus.domain.dto.UserCreate;
import com.codeemma.valueplus.domain.dto.data4Me.AgentDto;
import com.codeemma.valueplus.persistence.entity.Role;
import com.codeemma.valueplus.persistence.entity.User;
import com.codeemma.valueplus.persistence.repository.RoleRepository;
import com.codeemma.valueplus.persistence.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class RegistrationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final Data4meService data4meService;

    public RegistrationService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                               RoleRepository roleRepository, Data4meService data4meService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
        this.data4meService = data4meService;
    }

    public User saveUser(UserCreate userCreate, RoleType roleType) {
        User user = userRepository.save(User.from(userCreate)
                .role(getRole(roleType))
                .password(passwordEncoder.encode(userCreate.getPassword()))
                .enabled(true).build());

        data4meService.createAgent(AgentDto.from(userCreate)).ifPresent(v -> user.setAgentCode(v.getCode()));
        return userRepository.save(user);
    }

    private Role getRole(RoleType roleType) {
        return roleRepository.findByName(roleType.name())
                .orElse(
                        roleRepository.save(Role.builder().name(roleType.name()).build())
                );

    }

}
