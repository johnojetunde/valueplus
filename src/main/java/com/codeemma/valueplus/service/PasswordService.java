package com.codeemma.valueplus.service;

import com.codeemma.valueplus.dto.PasswordChange;
import com.codeemma.valueplus.exception.NotFoundException;
import com.codeemma.valueplus.model.User;
import com.codeemma.valueplus.repository.UserRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class PasswordService {
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    public PasswordService(PasswordEncoder passwordEncoder, UserRepository userRepository) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
    }

    public User changePassword(Long userId, PasswordChange passwordChange) {
        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("user not found"));
        if (!passwordEncoder.matches(passwordChange.getOldPassword(), user.getPassword())) {
            throw new BadCredentialsException("wrong password");
        }

        return userRepository.save(
                user.toBuilder()
                        .password(passwordEncoder.encode(passwordChange.getNewPassword()))
                        .build()
        );
    }
}
