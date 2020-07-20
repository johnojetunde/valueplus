package com.codeemma.valueplus.domain.util;

import com.codeemma.valueplus.persistence.entity.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;


public final class UserUtils {

    private UserUtils() {
    }
    public static User getLoggedInUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
            return null;
        }
        return ((User) authentication.getPrincipal());
    }
}
