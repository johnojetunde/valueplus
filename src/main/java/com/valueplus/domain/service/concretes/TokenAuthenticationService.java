package com.valueplus.domain.service.concretes;

import com.valueplus.app.model.UserAuthentication;
import com.valueplus.persistence.entity.TokenStore;
import com.valueplus.persistence.entity.User;
import com.valueplus.persistence.repository.TokenStoreRepository;
import com.valueplus.persistence.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

import static java.util.Optional.empty;

@Service
public class TokenAuthenticationService {

    public static final String AUTH_HEADER_NAME = "AUTHORIZATION";
    private final JwtGenerator jwtGenerator;
    private final UserRepository userRepository;
    private final TokenStoreRepository tokenStoreRepository;
    private final Long expiration;

    @Autowired
    public TokenAuthenticationService(JwtGenerator jwtGenerator,
                                      UserRepository userRepository,
                                      TokenStoreRepository tokenStoreRepository,
                                      @Value("${token.expiration}") Long expiration) {
        this.jwtGenerator = jwtGenerator;
        this.userRepository = userRepository;
        this.tokenStoreRepository = tokenStoreRepository;
        this.expiration = expiration;
    }

    public String generatorToken(User user) {
        final String token = jwtGenerator.generateToken(user);

        tokenStoreRepository.save(new TokenStore(token, getExpiryDate()));
        return token;
    }

    private LocalDateTime getExpiryDate() {
        return JwtGenerator.generateExpirationDate(expiration)
                .toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }

    public Optional<UserAuthentication> getAuthentication(HttpServletRequest request) {
        final String token = request.getHeader(AUTH_HEADER_NAME);
        if (token != null && tokenStoreRepository.findByToken(token).isPresent()) {
            String username = jwtGenerator.getUsernameFromToken(token);
            User user = userRepository.findByEmailAndDeletedFalse(username).orElse(null);
            if (user != null) {
                return Optional.of(new UserAuthentication(user));
            }
        }
        return empty();
    }
}

