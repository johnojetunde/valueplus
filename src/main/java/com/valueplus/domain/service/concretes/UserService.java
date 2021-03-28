package com.valueplus.domain.service.concretes;

import com.valueplus.app.exception.BadRequestException;
import com.valueplus.app.exception.NotFoundException;
import com.valueplus.app.exception.ValuePlusException;
import com.valueplus.app.exception.ValuePlusRuntimeException;
import com.valueplus.domain.model.*;
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
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.valueplus.domain.model.RoleType.ADMIN;
import static com.valueplus.domain.util.FunctionUtil.emptyIfNullStream;
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

        existingUser.setLastname(user.getLastname());
        existingUser.setFirstname(user.getFirstname());
        existingUser.setPhone(user.getPhone());
        existingUser.setAddress(user.getAddress());

        return userRepository.save(user);
    }

    public User updateUserAuthority(Long userid, Set<Long> userAuthorities) {
        User user = userRepository.findById(userid)
                .orElseThrow(() -> new NotFoundException("user not found"));

        if (!ADMIN.name().equals(user.getRole().getName())) {
            throw new BadRequestException("Authority update only applies to admin users");
        }

        List<Authority> authorityEntity = userUtilService.getAdminAuthority(userAuthorities);
        user.setAuthorities(authorityEntity);

        return userRepository.save(user);
    }

    public Set<AuthorityModel> getAllAuthorities() {
        return userUtilService.getAllAuthorities();
    }

    public User pinUpdate(Long userId, PinUpdate pinUpdate) throws ValuePlusException {
        User user = getUserById(userId);
        var pinUpdateService = getUpdateService(user);
        user = pinUpdateService.updateOrCreatePin(user, pinUpdate);

        return userRepository.save(user);
    }

    private User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("user not found"));
    }

    public void deleteUser(Long userId) {
        userRepository.deleteUser(userId);
    }

    public Optional<User> getAdminUserAccount() {
        return userRepository.findByEmailAndDeletedFalse("vpadmin@gmail.com");
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
                .map(AgentDto::valueOf);
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
