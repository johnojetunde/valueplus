package com.codeemma.valueplus.domain.mail;

import com.codeemma.valueplus.persistence.entity.User;

public interface EmailService {
    void sendPasswordReset(User user, String newPassword);
}
