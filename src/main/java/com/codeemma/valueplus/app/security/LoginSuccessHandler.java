package com.codeemma.valueplus.app.security;

import com.codeemma.valueplus.persistence.entity.User;
import org.springframework.security.core.Authentication;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class LoginSuccessHandler {
    public static void handleSuccessLogin(HttpServletRequest request,
                                          HttpServletResponse response,
                                          Authentication authentication,
                                          CustomUserService userService,
                                          TokenAuthenticationService tokenAuthenticationService) throws ServletException, IOException {
        final User authenticatedUser = userService.loadUserByUsername(authentication.getName());
        final UserAuthentication userAuthentication = new UserAuthentication(authenticatedUser);
        tokenAuthenticationService.addAuthentication(response, userAuthentication);
    }
}
