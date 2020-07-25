package com.codeemma.valueplus.persistence.repository;

import com.codeemma.valueplus.persistence.entity.EmailVerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, Long> {
    Optional<EmailVerificationToken> findByVerificationToken(String token);
}
