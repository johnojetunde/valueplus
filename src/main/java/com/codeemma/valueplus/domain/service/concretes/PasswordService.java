package com.codeemma.valueplus.domain.service.concretes;

import com.codeemma.valueplus.app.exception.NotFoundException;
import com.codeemma.valueplus.app.exception.ValuePlusException;
import com.codeemma.valueplus.domain.mail.EmailService;
import com.codeemma.valueplus.domain.model.NewPassword;
import com.codeemma.valueplus.domain.model.PasswordChange;
import com.codeemma.valueplus.domain.model.PasswordReset;
import com.codeemma.valueplus.domain.util.GeneratorUtils;
import com.codeemma.valueplus.persistence.entity.PasswordResetToken;
import com.codeemma.valueplus.persistence.entity.User;
import com.codeemma.valueplus.persistence.repository.PasswordResetTokenRepository;
import com.codeemma.valueplus.persistence.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static com.codeemma.valueplus.domain.model.RoleType.AGENT;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Slf4j
@Service
public class PasswordService {
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final String adminPasswordResetLink;
    private final String userPasswordResetLink;

    public PasswordService(PasswordEncoder passwordEncoder, UserRepository userRepository, EmailService emailService,
                           PasswordResetTokenRepository passwordResetTokenRepository,
                           @Value("${valueplus.admin.reset-password}") String adminPasswordResetLink,
                           @Value("${valueplus.user.reset-password}") String userPasswordResetLink
    ) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.adminPasswordResetLink = adminPasswordResetLink;
        this.userPasswordResetLink = userPasswordResetLink;
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

    public void sendResetPassword(PasswordReset passwordReset) throws Exception {
        User user = userRepository.findByEmailAndDeletedFalse(passwordReset.getEmail())
                .orElseThrow(() -> new NotFoundException("user not found"));

        String token = GeneratorUtils.generateRandomString(16);
        PasswordResetToken resetToken = new PasswordResetToken(user.getId(), token);
        passwordResetTokenRepository.save(resetToken);

        String resetLink = (AGENT.name().equals(user.getRole().getName()))
                ? userPasswordResetLink
                : adminPasswordResetLink;

        emailService.sendPasswordReset(user, resetLink.concat(token));
    }

    public User resetPassword(NewPassword newPassword) throws Exception {
        Optional<PasswordResetToken> resetToken = passwordResetTokenRepository.findByResetToken(newPassword.getResetToken());
        if (resetToken.isEmpty()) {
            log.error("password rest token, token {}", newPassword.getResetToken());
            throw new ValuePlusException("expired link", NOT_FOUND);
        }

        Long userId = resetToken.get().getUserId();
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            log.error("user not found, userId = {}", newPassword.getResetToken());
            throw new ValuePlusException("expired link", NOT_FOUND);
        }

        User user = userOptional.get();
        user = user.toBuilder()
                .password(passwordEncoder.encode(newPassword.getNewPassword())).build();
        userRepository.save(user);
        passwordResetTokenRepository.deleteById(userId);

        return user;
    }
}
