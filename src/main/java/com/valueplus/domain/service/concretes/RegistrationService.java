package com.valueplus.domain.service.concretes;

import com.valueplus.app.exception.ValuePlusException;
import com.valueplus.domain.model.AgentCreate;
import com.valueplus.domain.model.RoleType;
import com.valueplus.domain.model.UserCreate;
import com.valueplus.domain.model.data4Me.AgentCode;
import com.valueplus.domain.model.data4Me.Data4meAgentDto;
import com.valueplus.domain.service.abstracts.WalletService;
import com.valueplus.persistence.entity.Authority;
import com.valueplus.persistence.entity.Role;
import com.valueplus.persistence.entity.User;
import com.valueplus.persistence.repository.RoleRepository;
import com.valueplus.persistence.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import static com.valueplus.domain.model.RoleType.AGENT;
import static com.valueplus.domain.model.RoleType.SUPER_AGENT;
import static com.valueplus.domain.util.GeneratorUtils.generateRandomString;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

@RequiredArgsConstructor
@Slf4j
@Service
public class RegistrationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final Data4meService data4meService;
    private final EmailVerificationService emailVerificationService;
    private final WalletService walletService;
    private final UserUtilService userUtilService;


    public User createAgent(AgentCreate agentCreate) throws Exception {
        ensureUserIsUnique(agentCreate.getEmail().toLowerCase());

        User user = userRepository.save(User.from(agentCreate)
                .role(getRole(AGENT))
                .password(passwordEncoder.encode(agentCreate.getPassword()))
                .enabled(true)
                .build());

        var superAgent = userRepository.findByReferralCode(agentCreate.getSuperAgentCode());
        superAgent.ifPresent(user::setSuperAgent);

        Optional<AgentCode> agentOptional = data4meService.createAgent(Data4meAgentDto.from(agentCreate));
        agentOptional.ifPresent(agent -> user.setAgentCode(agent.getCode()));

        User savedUser = userRepository.save(user);
        walletService.createWallet(savedUser);
        emailVerificationService.sendVerifyEmail(user);
        return user;
    }

    public User createAdmin(UserCreate userCreate, RoleType roleType) throws Exception {
        ensureAuthorityIsPresent(userCreate);
        ensureUserIsUnique(userCreate.getEmail());
        List<Authority> authorities = userUtilService.getAdminAuthority(userCreate.getAuthorityIds());

        String password = generateRandomString(10);
        User user = newUserWithGeneratedPassword(userCreate, roleType, password);
        user.setAuthorities(authorities);

        user = userRepository.save(user);
        walletService.createWallet(user);
        emailVerificationService.sendAdminAccountCreationNotification(user, password);
        return user;
    }

    private void ensureAuthorityIsPresent(UserCreate userCreate) throws ValuePlusException {
        if (userCreate.getAuthorityIds() == null || userCreate.getAuthorityIds().size() == 0) {
            throw new ValuePlusException("Authority is required for an admin user", BAD_REQUEST);
        }
    }

    public User createSuperAgent(UserCreate userCreate) throws Exception {
        ensureUserIsUnique(userCreate.getEmail());

        String password = generateRandomString(10);
        String referralCode = generateRandomString(6);

        User user = newUserWithGeneratedPassword(userCreate, SUPER_AGENT, password);
        user.setReferralCode(referralCode);

        user = userRepository.save(user);
        walletService.createWallet(user);
        emailVerificationService.sendSuperAgentAccountCreationNotification(user, password);
        return user;
    }

    private User newUserWithGeneratedPassword(UserCreate userCreate, RoleType roleType, String password) {
        return userRepository.save(User.from(userCreate)
                .role(getRole(roleType))
                .password(passwordEncoder.encode(password))
                .enabled(true)
                .emailVerified(true)
                .superAgent(null)
                .build());
    }

    private void ensureUserIsUnique(String email) throws ValuePlusException {
        if (userRepository.findByEmailAndDeletedFalse(email)
                .isPresent()) {
            throw new ValuePlusException("User profile exists", HttpStatus.BAD_REQUEST);
        }
    }

    private Role getRole(RoleType roleType) {
        Optional<Role> optionalRole = roleRepository.findByName(roleType.name());
        return optionalRole
                .orElseGet(() -> roleRepository.save(Role.builder()
                        .name(roleType.name())
                        .build()));
    }
}
