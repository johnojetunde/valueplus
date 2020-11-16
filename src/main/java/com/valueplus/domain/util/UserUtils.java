package com.valueplus.domain.util;

import com.valueplus.app.exception.NotFoundException;
import com.valueplus.persistence.entity.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import static com.valueplus.domain.model.RoleType.ADMIN;
import static com.valueplus.domain.model.RoleType.AGENT;


public final class UserUtils {

    private UserUtils() {
    }

    public static User getLoggedInUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
            throw new NotFoundException("user not found");
        }
        return ((User) authentication.getPrincipal());
    }

    public static boolean isAgent(User user) {
        return AGENT.name().equals(user.getRole().getName());
    }

    public static boolean isAdmin(User user) {
        return ADMIN.name().equals(user.getRole().getName());
    }
}
