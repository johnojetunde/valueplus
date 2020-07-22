package com.codeemma.valueplus.app.security;

import com.codeemma.valueplus.persistence.entity.User;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface CustomUserService extends UserDetailsService {
    User loadUserByUsername(String username);
}
