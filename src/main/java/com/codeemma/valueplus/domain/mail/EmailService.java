package com.codeemma.valueplus.domain.mail;

import com.codeemma.valueplus.persistence.entity.User;

import java.math.BigDecimal;

public interface EmailService {
    void sendPasswordReset(User user, String newPassword) throws Exception;

    void sendEmailVerification(User user, String link) throws Exception;

    void sendAdminUserCreationEmail(User user, String password) throws Exception;

    void sendCreditNotification(User user, BigDecimal amount) throws Exception;

    void sendDebitNotification(User user, BigDecimal amount) throws Exception;
}
