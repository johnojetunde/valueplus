package com.valueplus.domain.service.concretes;

import com.valueplus.app.exception.BadRequestException;
import com.valueplus.app.exception.NotFoundException;
import com.valueplus.app.exception.ValuePlusException;
import com.valueplus.app.exception.ValuePlusRuntimeException;
import com.valueplus.domain.model.AuthorityModel;
import com.valueplus.domain.model.PinUpdate;
import com.valueplus.domain.model.RoleType;
import com.valueplus.domain.service.abstracts.PinUpdateService;
import com.valueplus.persistence.entity.Authority;
import com.valueplus.persistence.entity.User;
import com.valueplus.persistence.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.valueplus.domain.model.RoleType.ADMIN;
import static com.valueplus.domain.model.RoleType.SUPER_ADMIN;
import static com.valueplus.domain.util.FunctionUtil.emptyIfNullStream;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;
    private final List<PinUpdateService> pinUpdateServiceList;
    private final UserUtilService userUtilService;

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

    public User update(User user) {
        User existingUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new NotFoundException("user not found"));

        var userEntity = existingUser.toBuilder().lastname(user.getLastname())
                .firstname(user.getFirstname())
                .phone(user.getPhone())
                .address(user.getAddress())
                .build();

        return userRepository.save(userEntity);
    }

    public User updateUserAuthority(Long userid, Set<Long> userAuthorities) {
        User user = userRepository.findById(userid)
                .orElseThrow(() -> new NotFoundException("user not found"));

        if (!ADMIN.name().equals(user.getRole().getName()) && !SUPER_ADMIN.name().equals(user.getRole().getName())) {
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

    public List<User> findUserBySuperAgent(User superAgent) {
        return userRepository.findUserBySuperAgent(superAgent);
    }

    private PinUpdateService getUpdateService(User user) {
        return emptyIfNullStream(pinUpdateServiceList)
                .filter(p -> p.useStrategy(user))
                .findFirst()
                .orElseThrow(() -> new ValuePlusRuntimeException("Error retrieving implementation for PinUpdate"));
    }
}
