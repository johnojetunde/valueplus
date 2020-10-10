package com.codeemma.valueplus.domain.service.concretes;

import com.codeemma.valueplus.app.exception.ValuePlusException;
import com.codeemma.valueplus.domain.model.AgentCreate;
import com.codeemma.valueplus.domain.model.RoleType;
import com.codeemma.valueplus.domain.model.UserCreate;
import com.codeemma.valueplus.domain.model.data4Me.AgentCode;
import com.codeemma.valueplus.domain.model.data4Me.Data4meAgentDto;
import com.codeemma.valueplus.domain.service.abstracts.WalletService;
import com.codeemma.valueplus.persistence.entity.Role;
import com.codeemma.valueplus.persistence.entity.User;
import com.codeemma.valueplus.persistence.repository.RoleRepository;
import com.codeemma.valueplus.persistence.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static com.codeemma.valueplus.domain.model.RoleType.AGENT;
import static com.codeemma.valueplus.domain.util.GeneratorUtils.generateRandomString;

@Service
public class RegistrationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final Data4meService data4meService;
    private final EmailVerificationService emailVerificationService;
    private final WalletService walletService;

    public RegistrationService(UserRepository userRepository,
                               PasswordEncoder passwordEncoder,
                               RoleRepository roleRepository,
                               Data4meService data4meService,
                               EmailVerificationService emailVerificationService,
                               WalletService walletService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
        this.data4meService = data4meService;
        this.emailVerificationService = emailVerificationService;
        this.walletService = walletService;
    }

    public User createAgent(AgentCreate agentCreate) throws Exception {
        if (userRepository.findByEmailAndDeletedFalse(agentCreate.getEmail())
                .isPresent()) {
            throw new ValuePlusException("User profile exists", HttpStatus.BAD_REQUEST);
        }

        User user = userRepository.save(User.from(agentCreate)
                .role(getRole(AGENT))
                .password(passwordEncoder.encode(agentCreate.getPassword()))
                .enabled(true)
                .build());

        Optional<AgentCode> agentOptional = data4meService.createAgent(Data4meAgentDto.from(agentCreate));
        agentOptional.ifPresent(agent -> user.setAgentCode(agent.getCode()));

        User savedUser = userRepository.save(user);
        walletService.createWallet(savedUser);
        emailVerificationService.sendVerifyEmail(user);
        return user;
    }

    public User createAdmin(UserCreate userCreate, RoleType roleType) throws Exception {
        if (userRepository.findByEmailAndDeletedFalse(userCreate.getEmail())
                .isPresent()) {
            throw new ValuePlusException("User profile exists", HttpStatus.BAD_REQUEST);
        }

        String password = generateRandomString(10);
        User user = userRepository.save(User.from(userCreate)
                .role(getRole(roleType))
                .password(passwordEncoder.encode(password))
                .enabled(true)
                .emailVerified(true)
                .build());

        user = userRepository.save(user);
        walletService.createWallet(user);
        emailVerificationService.sendAdminAccountCreationNotification(user, password);
        return user;
    }

    private Role getRole(RoleType roleType) {
        Optional<Role> optionalRole = roleRepository.findByName(roleType.name());
        return optionalRole
                .orElseGet(() -> roleRepository.save(Role.builder()
                        .name(roleType.name())
                        .build()));
    }
}
