package com.codeemma.valueplus.domain.service.concretes;

import com.codeemma.valueplus.domain.dto.PasswordChange;
import com.codeemma.valueplus.app.exception.NotFoundException;
import com.codeemma.valueplus.domain.dto.PasswordReset;
import com.codeemma.valueplus.domain.mail.EmailService;
import com.codeemma.valueplus.domain.util.GeneratorUtils;
import com.codeemma.valueplus.persistence.entity.User;
import com.codeemma.valueplus.persistence.repository.UserRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class PasswordService {
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final EmailService emailService;

    public PasswordService(PasswordEncoder passwordEncoder, UserRepository userRepository, EmailService emailService) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.emailService = emailService;
    }

    public User changePassword(Long userId, PasswordChange passwordChange) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("user not found"));
        if (!passwordEncoder.matches(passwordChange.getOldPassword(), user.getPassword())) {
            throw new BadCredentialsException("wrong password");
        }

        return userRepository.save(
                user.toBuilder()
                        .password(passwordEncoder.encode(passwordChange.getNewPassword()))
                        .build()
        );
    }

    public void resetPassword(PasswordReset passwordReset) throws Exception {
        User user = userRepository.findByEmailAndDeletedFalse(passwordReset.getEmail())
                .orElseThrow(() -> new NotFoundException("user not found"));

        String newPassword = GeneratorUtils.generateRandomString(8);

        user = userRepository.save(
                user.toBuilder()
                        .passwordReset(true)
                        .password(passwordEncoder.encode(newPassword))
                        .build()
        );

        emailService.sendPasswordReset(user, newPassword);
    }
}
