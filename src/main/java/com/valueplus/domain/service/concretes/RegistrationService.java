package com.valueplus.domain.service.concretes;

import com.valueplus.app.config.audit.AuditEventPublisher;
import com.valueplus.app.exception.ValuePlusException;
import com.valueplus.domain.enums.ProductProvider;
import com.valueplus.domain.model.AgentCreate;
import com.valueplus.domain.model.RoleType;
import com.valueplus.domain.model.UserCreate;
import com.valueplus.domain.products.ProductProviderService;
import com.valueplus.domain.products.ProductProviderUrlService;
import com.valueplus.domain.products.ProductProviderUserModel;
import com.valueplus.domain.service.abstracts.WalletService;
import com.valueplus.persistence.entity.Authority;
import com.valueplus.persistence.entity.ProductProviderUser;
import com.valueplus.persistence.entity.Role;
import com.valueplus.persistence.entity.User;
import com.valueplus.persistence.repository.ProductProviderUserRepository;
import com.valueplus.persistence.repository.RoleRepository;
import com.valueplus.persistence.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.valueplus.domain.enums.ActionType.*;
import static com.valueplus.domain.enums.EntityType.USER;
import static com.valueplus.domain.model.RoleType.AGENT;
import static com.valueplus.domain.model.RoleType.SUPER_AGENT;
import static com.valueplus.domain.util.FunctionUtil.emptyIfNullStream;
import static com.valueplus.domain.util.GeneratorUtils.generateRandomString;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

@RequiredArgsConstructor
@Slf4j
@Service
public class RegistrationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final EmailVerificationService emailVerificationService;
    private final WalletService walletService;
    private final UserUtilService userUtilService;
    private final AuditEventPublisher auditEvent;
    private final List<ProductProviderService> providerServices;
    private final ProductProviderUserRepository providerRepository;

    public User createAgent(AgentCreate agentCreate) throws Exception {
        try {
            ensureUserIsUnique(agentCreate.getEmail().toLowerCase());

            User user = userRepository.save(User.from(agentCreate)
                    .role(getRole(AGENT))
                    .password(passwordEncoder.encode(agentCreate.getPassword()))
                    .enabled(true)
                    .build());

            var superAgent = Optional.ofNullable(agentCreate.getSuperAgentCode())
                    .flatMap(userRepository::findByReferralCode);

            superAgent.ifPresent(user::setSuperAgent);

            List<ProductProviderUser> productProviders = registerWithProductProviders(agentCreate);

            User savedUser = userRepository.save(user);
            var providersWithUser = emptyIfNullStream(productProviders)
                    .map(p -> p.setUser(savedUser))
                    .collect(Collectors.toList());

            user.setProductProviders(providersWithUser);

            providerRepository.saveAll(productProviders);
            walletService.createWallet(savedUser);
            emailVerificationService.sendVerifyEmail(user);

            auditEvent.publish(new Object(), savedUser, USER_CREATE_AGENT, USER);
            return savedUser;
        } catch (Exception e) {
            log.error("error", e);
            throw e;
        }
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
        auditEvent.publish(new Object(), user, USER_CREATE_ADMIN, USER);
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
        String referralCode = generateRandomString(8);

        User user = newUserWithGeneratedPassword(userCreate, SUPER_AGENT, password);
        user.setReferralCode(referralCode.toLowerCase());

        user = userRepository.save(user);
        walletService.createWallet(user);
        emailVerificationService.sendSuperAgentAccountCreationNotification(user, password);
        auditEvent.publish(new Object(), user, USER_CREATE_SUPER_AGENT, USER);
        return user;
    }

    public Map<ProductProvider, ProductProviderUrlService> productUrlProvider() {
        return userUtilService.productUrlProvider();
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

    private List<ProductProviderUser> registerWithProductProviders(AgentCreate agentCreate) {
        var productProviderUser = ProductProviderUserModel.from(agentCreate);
        return emptyIfNullStream(providerServices)
                .map(p -> p.register(productProviderUser))
                .map(ProductProviderUser::toNewEntity)
                .collect(Collectors.toList());
    }
}
