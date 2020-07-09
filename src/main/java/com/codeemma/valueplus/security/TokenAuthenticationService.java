package com.codeemma.valueplus.security;

import com.codeemma.valueplus.model.User;
import com.codeemma.valueplus.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Service
public class TokenAuthenticationService {

    public static final String AUTH_HEADER_NAME = "AUTHORIZATION";
    private static final String AUTH_COOKIE_NAME = "AUTH-TOKEN";
    public static final long TEN_DAYS = 1000 * 60 * 60 * 24 * 10;

    private final JwtUtils jwtUtils;
    private UserRepository userRepository;

    @Autowired
    public TokenAuthenticationService(JwtUtils jwtUtils, UserRepository userRepository) {
        this.jwtUtils = jwtUtils;
        this.userRepository = userRepository;
    }

    public void addAuthentication(HttpServletResponse response,
                                  UserAuthentication authentication) throws IOException {
        final User user = authentication.getDetails();
        final String token = jwtUtils.generateToken(user);

        // Put the token into a cookie because the client can't capture response
        // headers of redirects / full page reloads.
        // (Its reloaded as a result of this response triggering a redirect back to "/")
        response.addHeader(AUTH_HEADER_NAME, token);
        response.addCookie(createCookieForToken(token));
        String userData = new ObjectMapper().writeValueAsString(user);
        response.setContentType("application/json");
        response.getWriter().write(userData);
    }

    public String createUserToken(User user) {
        return jwtUtils.generateToken(user);
    }

    public UserAuthentication getAuthentication(HttpServletRequest request) {
        // to prevent CSRF attacks we still only allow authentication using a custom HTTP header
        // (it is up to the client to read our previously set cookie and put it in the header)
        final String token = request.getHeader(AUTH_HEADER_NAME);
        if (token != null) {
            String username = jwtUtils.getUsernameFromToken(token);
            User user = userRepository.findByEmailOrUsernameAndDeletedFalse(username);
            if(user != null){
                return new UserAuthentication(user);
            }
        }
        return null;
    }

    private Cookie createCookieForToken(String token) {
        final Cookie authCookie = new Cookie(AUTH_COOKIE_NAME, token);
        authCookie.setPath("/");
        return authCookie;
    }
}

