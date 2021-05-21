package com.valueplus.app.config.security;

import com.valueplus.app.model.UserAuthentication;
import com.valueplus.domain.service.concretes.TokenAuthenticationService;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

import static java.util.Collections.emptyList;

@Component
public class AuthenticationFilter extends OncePerRequestFilter {

    private final TokenAuthenticationService tokenAuthenticationService;

    public AuthenticationFilter(TokenAuthenticationService tokenAuthenticationService) {
        super();
        this.tokenAuthenticationService = tokenAuthenticationService;
    }

    @Override
    public void doFilterInternal(HttpServletRequest httpServletRequest,
                                 HttpServletResponse httpServletResponse,
                                 FilterChain chain) throws ServletException, IOException {

        if (HttpMethod.OPTIONS.toString().equals(httpServletRequest.getMethod())) {
            User user = new User("test_user", "test_password", emptyList());

            UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
                    user, null, user.getAuthorities());

            SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
            chain.doFilter(httpServletRequest, httpServletResponse);
            return;
        }

        try {
            Optional<UserAuthentication> authentication = tokenAuthenticationService.getAuthentication(httpServletRequest);

            if (authentication.isPresent()) {
                var usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
                        authentication.get().getDetails(),
                        null,
                        authentication.get().getAuthorities());

                SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
            }

            chain.doFilter(httpServletRequest, httpServletResponse);
        } catch (Exception e) {
            logger.info(e.getMessage());
            httpServletResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            httpServletResponse.getWriter().write("Invalid credentials");
        }
    }
}