package com.codeemma.valueplus.app.security;

import com.codeemma.valueplus.domain.model.LoginToken;
import com.codeemma.valueplus.persistence.entity.TokenStore;
import com.codeemma.valueplus.persistence.entity.User;
import com.codeemma.valueplus.persistence.repository.TokenStoreRepository;
import com.codeemma.valueplus.persistence.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static com.codeemma.valueplus.app.security.JwtUtils.generateExpirationDate;

@Service
public class TokenAuthenticationService {

    public static final String AUTH_HEADER_NAME = "AUTHORIZATION";
    private final JwtUtils jwtUtils;
    private final UserRepository userRepository;
    private final TokenStoreRepository tokenStoreRepository;
    private final Long expiration;

    @Autowired
    public TokenAuthenticationService(JwtUtils jwtUtils,
                                      UserRepository userRepository,
                                      TokenStoreRepository tokenStoreRepository,
                                      @Value("${token.expiration}") Long expiration) {
        this.jwtUtils = jwtUtils;
        this.userRepository = userRepository;
        this.tokenStoreRepository = tokenStoreRepository;
        this.expiration = expiration;
    }

    public void addAuthentication(HttpServletResponse response,
                                  UserAuthentication authentication) throws IOException {
        final User user = authentication.getDetails();
        final String token = jwtUtils.generateToken(user);

        tokenStoreRepository.save(new TokenStore(token, getExpiryDate()));

        String loginToken = new ObjectMapper().writeValueAsString(new LoginToken(token));
        response.setContentType("application/json");
        response.getWriter().write(loginToken);
    }

    private LocalDateTime getExpiryDate() {
        return generateExpirationDate(expiration)
                .toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }

    public UserAuthentication getAuthentication(HttpServletRequest request) {
        final String token = request.getHeader(AUTH_HEADER_NAME);
        if (token != null && tokenStoreRepository.findByToken(token).isPresent()) {
            String username = jwtUtils.getUsernameFromToken(token);
            User user = userRepository.findByEmailAndDeletedFalse(username).orElse(null);
            if (user != null) {
                return new UserAuthentication(user);
            }
        }
        return null;
    }
}

