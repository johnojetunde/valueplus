package com.valueplus.domain.service.concretes;

import com.valueplus.app.config.audit.AuditEventPublisher;
import com.valueplus.app.exception.BadRequestException;
import com.valueplus.app.exception.NotFoundException;
import com.valueplus.app.exception.ValuePlusException;
import com.valueplus.app.exception.ValuePlusRuntimeException;
import com.valueplus.domain.enums.ProductProvider;
import com.valueplus.domain.mail.EmailService;
import com.valueplus.domain.model.*;
import com.valueplus.domain.products.ProductProviderUrlService;
import com.valueplus.domain.service.abstracts.PinUpdateService;
import com.valueplus.persistence.entity.Authority;
import com.valueplus.persistence.entity.Role;
import com.valueplus.persistence.entity.User;
import com.valueplus.persistence.repository.RoleRepository;
import com.valueplus.persistence.repository.UserRepository;
import com.valueplus.persistence.specs.SearchCriteria;
import com.valueplus.persistence.specs.SearchOperation;
import com.valueplus.persistence.specs.UserSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.valueplus.domain.enums.ActionType.*;
import static com.valueplus.domain.enums.EntityType.USER;
import static com.valueplus.domain.model.RoleType.ADMIN;
import static com.valueplus.domain.util.Constants.WHITE_LISTED_AUTHORITIES_UI;
import static com.valueplus.domain.util.FunctionUtil.emptyIfNullStream;
import static com.valueplus.domain.util.MapperUtil.copy;
import static java.time.LocalTime.MAX;
import static java.time.LocalTime.MIN;
import static java.util.Optional.ofNullable;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;
    private final List<PinUpdateService> pinUpdateServiceList;
    private final UserUtilService userUtilService;
    private final Clock clock;
    private final RoleRepository roleRepository;
    private final AuditEventPublisher auditEvent;
    private final EmailService emailService;

    private final static String ADMIN_ACCOUNT = "vpadmin@gmail.com";

    public Page<User> findUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    public Page<User> findSuperAgentUsers(Pageable pageable) {
        return userRepository.findUserByRole_Name(RoleType.SUPER_AGENT.name(), pageable);
    }

    public Optional<User> find(long userId) {
        return userRepository.findById(userId);
    }

    public Optional<User> findByReferralCode(String superAgentCode) {
        return userRepository.findByReferralCode(superAgentCode);
    }

    public Page<User> findAllUserBySuperAgentCode(String superAgentCode, Pageable pageable) {
        return userRepository.findUserBySuperAgent_ReferralCode(superAgentCode, pageable);
    }

    public Page<User> findAllUserBySuperAgentCode(String superAgentCode,
                                                  LocalDate startDate,
                                                  LocalDate endDate,
                                                  Pageable pageable) {
        LocalDate todayDate = LocalDate.now(clock);
        LocalDateTime startDateTime = ofNullable(startDate)
                .map(st -> LocalDateTime.of(startDate, MIN))
                .orElseGet(() -> LocalDateTime.of(todayDate.minusDays(30), MIN));
        LocalDateTime endDateTime = ofNullable(startDate)
                .map(st -> LocalDateTime.of(endDate, MAX))
                .orElseGet(() -> LocalDateTime.of(todayDate, MAX));

        return userRepository.findActiveSuperAgentUsers(startDateTime, endDateTime, superAgentCode, pageable);
    }

    public User update(User user) {
        User existingUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new NotFoundException("user not found"));

        var oldObject = copy(existingUser, User.class);

        existingUser.setLastname(user.getLastname());
        existingUser.setFirstname(user.getFirstname());
        existingUser.setPhone(user.getPhone());
        existingUser.setAddress(user.getAddress());

        var savedEntity = userRepository.save(user);
        auditEvent.publish(oldObject, savedEntity, USER_PROFILE_UPDATE, USER);
        return savedEntity;
    }

    public User updateUserAuthority(Long userid, Set<Long> userAuthorities) {
        User user = userRepository.findById(userid)
                .orElseThrow(() -> new NotFoundException("user not found"));
        var oldObject = copy(user, User.class);

        if (!ADMIN.name().equals(user.getRole().getName())) {
            throw new BadRequestException("Authority update only applies to admin users");
        }

        List<Authority> authorityEntity = userUtilService.getAdminAuthority(userAuthorities);
        user.setAuthorities(authorityEntity);

        var savedEntity = userRepository.save(user);
        auditEvent.publish(oldObject, savedEntity, USER_AUTHORITY_UPDATE, USER);
        return savedEntity;
    }

    public List<AuthorityModel> getAllAuthorities() {
        return userUtilService.getAllAuthorities();
    }

    public List<AuthorityModel> getAllUIAuthorities() {
        return getAllAuthorities().stream()
                .filter(au -> WHITE_LISTED_AUTHORITIES_UI.contains(au.getAuthority().toUpperCase()))
                .sorted(Comparator.comparing(AuthorityModel::getAuthority))
                .collect(Collectors.toList());
    }

    public User pinUpdate(Long userId, PinUpdate pinUpdate) throws Exception {
        User user = getUserById(userId);
        var oldObject = copy(user, User.class);
        var pinUpdateService = getUpdateService(user);
        user = pinUpdateService.updateOrCreatePin(user, pinUpdate);

        var savedEntity = userRepository.save(user);
        auditEvent.publish(oldObject, savedEntity, USER_PIN_UPDATE, USER);

        emailService.sendPinNotification(user);
        return savedEntity;
    }

    public User enableUser(Long userId) throws ValuePlusException {
        User user = getUserById(userId);
        if (user.isEnabled()) {
            throw new ValuePlusException("User is currently enabled", BAD_REQUEST);
        }

        var oldObject = copy(user, User.class);
        user.setEnabled(true);
        var savedEntity = userRepository.save(user);

        auditEvent.publish(oldObject, savedEntity, USER_ENABLE, USER);
        return savedEntity;
    }

    public User disableUser(Long userId) throws ValuePlusException {
        User user = getUserById(userId);
        if (!user.isEnabled()) {
            throw new ValuePlusException("User is currently disabled", BAD_REQUEST);
        }

        var oldObject = copy(user, User.class);
        user.setEnabled(false);
        var savedEntity = userRepository.save(user);

        auditEvent.publish(oldObject, savedEntity, USER_DISABLE, USER);
        return savedEntity;
    }

    private User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("user not found"));
    }

    public void deleteUser(Long userId) {
        userRepository.deleteUser(userId);
    }

    public Optional<User> getAdminUserAccount() {
        return userRepository.findByEmailAndDeletedFalse(ADMIN_ACCOUNT);
    }

    public Long getAdminUserId() {
        return getAdminUserAccount().map(User::getId).orElse(0L);
    }

    private PinUpdateService getUpdateService(User user) {
        return emptyIfNullStream(pinUpdateServiceList)
                .filter(p -> p.useStrategy(user))
                .findFirst()
                .orElseThrow(() -> new ValuePlusRuntimeException("Error retrieving implementation for PinUpdate"));
    }

    public Page<AgentDto> searchUsers(UserSearchFilter searchFilter, Pageable pageable) throws ValuePlusException {
        UserSpecification specification = buildSpecification(searchFilter);
        return userRepository.findAll(Specification.where(specification), pageable)
                .map(u -> AgentDto.valueOf(u, productUrlProvider()));
    }

    public Map<ProductProvider, ProductProviderUrlService> productUrlProvider() {
        return userUtilService.productUrlProvider();
    }

    private UserSpecification buildSpecification(UserSearchFilter searchFilter) throws ValuePlusException {
        UserSpecification specification = new UserSpecification();
        if (searchFilter.getFirstname() != null) {
            specification.add(new SearchCriteria<>("firstname", searchFilter.getFirstname(), SearchOperation.MATCH));
        }
        if (searchFilter.getLastname() != null) {
            specification.add(new SearchCriteria<>("lastname", searchFilter.getLastname(), SearchOperation.MATCH));
        }

        if (searchFilter.getRoleType() != null) {
            Role role = roleRepository.findByName(searchFilter.getRoleType().name()).get();
            specification.add(new SearchCriteria<>("role", role, SearchOperation.EQUAL));
        }
        if (searchFilter.getEmail() != null) {
            specification.add(new SearchCriteria<>("email", searchFilter.getEmail(), SearchOperation.EQUAL));
        }
        if (searchFilter.getSuperAgentCode() != null) {
            User superAgent = userRepository.findByReferralCode(searchFilter.getSuperAgentCode())
                    .orElseThrow(() -> new ValuePlusException("Invalid super Agent code ", BAD_REQUEST));
            specification.add(new SearchCriteria<>("superAgent", superAgent, SearchOperation.EQUAL));
        }

        return specification;
    }
}
