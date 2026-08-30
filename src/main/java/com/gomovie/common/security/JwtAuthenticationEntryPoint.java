package com.gomovie.common.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException)
            throws IOException {

        log.warn(
                "Authentication required for {} {}. Reason: {}",
                request.getMethod(),
                request.getRequestURI(),
                authException.getMessage()
        );

        response.sendError(
                HttpServletResponse.SC_UNAUTHORIZED,
                "Authentication required"
        );
    }
}