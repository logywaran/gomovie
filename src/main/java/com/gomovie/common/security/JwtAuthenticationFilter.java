package com.gomovie.common.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        log.info(
                "JWT filter processing {} {}",
                request.getMethod(),
                request.getRequestURI()
        );

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {

            log.info("No Bearer token found");

            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        try {

            String username = jwtService.extractUsername(token);

            log.info(
                    "JWT username extracted successfully: {}",
                    username
            );

            if (SecurityContextHolder.getContext().getAuthentication() == null) {

                UserDetails userDetails =
                        userDetailsService.loadUserByUsername(username);

                log.info(
                        "User loaded successfully: {}",
                        userDetails.getUsername()
                );

                boolean valid =
                        jwtService.isTokenValid(token, userDetails);

                log.info(
                        "JWT validation result for '{}': {}",
                        username,
                        valid
                );

                if (valid) {

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );

                    SecurityContextHolder
                            .getContext()
                            .setAuthentication(authentication);

                    log.info(
                            "Authentication set successfully. user={}, authorities={}",
                            userDetails.getUsername(),
                            userDetails.getAuthorities()
                    );
                }
            }

        } catch (Exception ex) {

            log.error(
                    "JWT authentication failed for {} {}",
                    request.getMethod(),
                    request.getRequestURI(),
                    ex
            );
        }

        log.info(
                "Authentication before continuing filter chain: {}",
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
        );

        filterChain.doFilter(request, response);
    }
}