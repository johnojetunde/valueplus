package com.codeemma.valueplus.service;

import com.codeemma.valueplus.dto.PasswordChange;
import com.codeemma.valueplus.dto.UserDto;
import com.codeemma.valueplus.exception.NotFoundException;
import com.codeemma.valueplus.model.User;
import com.codeemma.valueplus.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;


    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Page<User> findUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    public Optional<User> find(long userId) {
        return userRepository.findById(userId);
    }

    public User update(User user) {
        User existingUser = userRepository.findById(user.getId()).orElseThrow(() -> new NotFoundException("user not found"));
        User.UserBuilder builder = existingUser.toBuilder();

        builder.lastname(user.getLastname())
                .firstname(user.getFirstname())
                .phone(user.getPhone())
                .address(user.getAddress());
        return userRepository.save(builder.build());
    }

    public void deleteUser(Long userId) {
        userRepository.deleteUser(userId);
    }
}
