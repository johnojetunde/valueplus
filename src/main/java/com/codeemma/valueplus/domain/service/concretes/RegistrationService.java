package com.codeemma.valueplus.domain.service.concretes;

import com.codeemma.valueplus.app.exception.ValuePlusException;
import com.codeemma.valueplus.domain.dto.RoleType;
import com.codeemma.valueplus.domain.dto.UserCreate;
import com.codeemma.valueplus.domain.dto.data4Me.AgentCode;
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
    private final EmailVerificationService emailVerificationService;

    public RegistrationService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                               RoleRepository roleRepository, Data4meService data4meService, EmailVerificationService emailVerificationService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
        this.data4meService = data4meService;
        this.emailVerificationService = emailVerificationService;
    }

    public User saveUser(UserCreate userCreate, RoleType roleType) throws Exception {
        if (userRepository.findByEmailAndDeletedFalse(userCreate.getEmail())
                .isPresent()) {
            throw new ValuePlusException("User profile exists", HttpStatus.BAD_REQUEST);
        }

        User user = userRepository.save(User.from(userCreate)
                .role(getRole(roleType))
                .password(passwordEncoder.encode(userCreate.getPassword()))
                .enabled(true).build());

        Optional<AgentCode> agent = data4meService.createAgent(AgentDto.from(userCreate));
        if (agent.isPresent()) {
            user.setAgentCode(agent.get().getCode());
            userRepository.save(user);
        }
        emailVerificationService.sendVerifyEmail(user);
        return user;
    }

    private Role getRole(RoleType roleType) {
        Optional<Role> optionalRole = roleRepository.findByName(roleType.name());

        if (optionalRole.isPresent()) {
            return optionalRole.get();
        }

        return roleRepository.save(Role.builder()
                .name(roleType.name())
                .build());
    }
}
