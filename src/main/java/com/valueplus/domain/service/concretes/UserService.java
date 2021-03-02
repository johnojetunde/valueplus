package com.valueplus.domain.service.concretes;

import com.valueplus.app.exception.NotFoundException;
import com.valueplus.app.exception.ValuePlusException;
import com.valueplus.app.exception.ValuePlusRuntimeException;
import com.valueplus.domain.model.PinUpdate;
import com.valueplus.domain.service.abstracts.PinUpdateService;
import com.valueplus.persistence.entity.User;
import com.valueplus.persistence.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import static com.valueplus.domain.util.FunctionUtil.emptyIfNullStream;

@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;
    private final List<PinUpdateService> pinUpdateServiceList;

    public Page<User> findUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    public Optional<User> find(long userId) {
        return userRepository.findById(userId);
    }

    public User update(User user) {
        User existingUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new NotFoundException("user not found"));
        User.UserBuilder builder = existingUser.toBuilder();

        builder.lastname(user.getLastname())
                .firstname(user.getFirstname())
                .phone(user.getPhone())
                .address(user.getAddress());
        return userRepository.save(builder.build());
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
}
