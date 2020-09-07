package com.codeemma.valueplus.domain.mail;

import com.codeemma.valueplus.persistence.entity.User;

public interface EmailService {
    void sendPasswordReset(User user, String newPassword) throws Exception;

    void sendEmailVerification(User user, String link) throws Exception;

    void sendAdminUserCreationEmail(User user, String password) throws Exception;
}
