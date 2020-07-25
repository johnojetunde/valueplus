package com.codeemma.valueplus.domain.mail;

import com.codeemma.valueplus.persistence.entity.User;

import javax.mail.MessagingException;

public interface EmailService {
    void sendPasswordReset(User user, String newPassword) throws Exception;

    void sendEmailVerification(User user, String link) throws Exception;
}
