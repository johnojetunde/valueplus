package com.codeemma.valueplus.security;

import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException e) throws IOException, ServletException {
        String error = response.getHeader("error");
        response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, error != null ? error:"Unauthorized");
    }
}
