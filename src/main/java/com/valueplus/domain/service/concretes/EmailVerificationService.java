package com.valueplus.domain.service.concretes;

import com.valueplus.app.exception.ValuePlusException;
import com.valueplus.domain.mail.EmailService;
import com.valueplus.domain.util.GeneratorUtils;
import com.valueplus.persistence.entity.EmailVerificationToken;
import com.valueplus.persistence.entity.User;
import com.valueplus.persistence.repository.EmailVerificationTokenRepository;
import com.valueplus.persistence.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
public class EmailVerificationService {
    private final EmailVerificationTokenRepository verificationTokenRepository;
    private final EmailService emailService;
    private final UserRepository userRepository;
    private final String verifyEmailLink;

    public EmailVerificationService(EmailVerificationTokenRepository verificationTokenRepository,
                                    EmailService emailService,
                                    UserRepository userRepository,
                                    @Value("${valueplus.verify-email}") String verifyEmailLink) {
        this.verificationTokenRepository = verificationTokenRepository;
        this.emailService = emailService;
        this.userRepository = userRepository;
        this.verifyEmailLink = verifyEmailLink;
    }

    public void sendVerifyEmail(User user) throws Exception {
        log.debug("sending verification to user");
        String token = GeneratorUtils.generateRandomString(16);

        EmailVerificationToken emailVerificationToken = new EmailVerificationToken(user.getId(), token);
        verificationTokenRepository.save(emailVerificationToken);
        emailService.sendEmailVerification(user, verifyEmailLink.concat(emailVerificationToken.getVerificationToken()));
    }

    public void sendAdminAccountCreationNotification(User user, String password) throws Exception {
        emailService.sendAdminUserCreationEmail(user, password);
    }

    public User confirmEmail(String token) throws Exception {

        Optional<EmailVerificationToken> emailVerificationToken = verificationTokenRepository.findByVerificationToken(token);
        if (emailVerificationToken.isEmpty()) {
            log.error("email verification token not found, token = {}", token);

            throw new ValuePlusException("expired link", HttpStatus.NOT_FOUND);
        }

        Long userId = emailVerificationToken.get().getUserId();
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            log.error("user not found, userId = {}", userId);
            throw new ValuePlusException("expired link", HttpStatus.NOT_FOUND);
        }
        User user = userOptional.get();
        user = user.toBuilder().emailVerified(true).build();
        userRepository.save(user);
        verificationTokenRepository.deleteById(userId);

        return user;
    }
}
