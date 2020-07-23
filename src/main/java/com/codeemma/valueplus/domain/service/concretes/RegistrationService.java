package com.codeemma.valueplus.domain.service.concretes;

import com.codeemma.valueplus.app.exception.ValuePlusException;
import com.codeemma.valueplus.domain.dto.RoleType;
import com.codeemma.valueplus.domain.dto.UserCreate;
import com.codeemma.valueplus.domain.dto.data4Me.AgentDto;
import com.codeemma.valueplus.persistence.entity.Role;
import com.codeemma.valueplus.persistence.entity.User;
import com.codeemma.valueplus.persistence.repository.RoleRepository;
import com.codeemma.valueplus.persistence.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

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

    public User saveUser(UserCreate userCreate, RoleType roleType) throws ValuePlusException {
        if (userRepository.findByEmailAndDeletedFalse(userCreate.getEmail())
                .isPresent()) {
            throw new ValuePlusException("User profile exists", HttpStatus.BAD_REQUEST);
        }

        User user = userRepository.save(User.from(userCreate)
                .role(getRole(roleType))
                .password(passwordEncoder.encode(userCreate.getPassword()))
                .enabled(true).build());

        data4meService.createAgent(AgentDto.from(userCreate))
                .ifPresent(v -> user.setAgentCode(v.getCode()));
        return userRepository.save(user);
    }

    private Role getRole(RoleType roleType) {
        Optional<Role> optionalRole = roleRepository.findByName(roleType.name());

        if (optionalRole.isEmpty()) {
            return roleRepository.save(Role.builder().name(roleType.name()).build());
        }

        return optionalRole.get();
    }
}
