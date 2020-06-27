package com.codeemma.valueplus.security;

import com.codeemma.valueplus.model.User;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface CustomUserService extends UserDetailsService {
    User loadUserByUsername(String username);
}
