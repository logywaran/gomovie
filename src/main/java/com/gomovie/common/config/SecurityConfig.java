package com.gomovie.common.config;

import com.gomovie.common.security.JwtAuthenticationEntryPoint;
import com.gomovie.common.security.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http
                .csrf(csrf -> csrf.disable())

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                .authorizeHttpRequests(auth -> auth

                        // ==========================
                        // Authentication
                        // ==========================
                        .requestMatchers(
                                "/api/users/register",
                                "/api/users/login"
                        )
                        .permitAll()

                        // ==========================
                        // Public APIs
                        // ==========================
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/cities",
                                "/api/cities/**",
                                "/api/movies/**",
                                "/api/shows/**",
                                "/api/theatres/**",
                                "/api/screens/**",
                                "/swagger-ui/**",
                                "/v3/api-docs/**"
                        )
                        .permitAll()

                        // ==========================
                        // Theatre Management
                        // ==========================
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/theatres/*/screens"
                        )
                        .hasRole("THEATRE_MANAGER")

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/screens/*/seats"
                        )
                        .hasRole("THEATRE_MANAGER")

                        // ==========================
                        // Admin APIs
                        // ==========================
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/cities"
                        )
                        .hasRole("ADMIN")

                        // ==========================
                        // Customer APIs
                        // ==========================
                        .requestMatchers(
                                "/api/bookings/**"
                        )
                        .hasRole("CUSTOMER")

                                // ==========================
                                // Mobi Payment APIs
                                // ==========================
                                .requestMatchers(
                                        "/api/payments/mobi/deposit",
                                        "/api/payments/mobi/redirect"
                                )
                                .permitAll()

                                // ==========================
                                // Public Payment Pages
                                // ==========================
                                .requestMatchers(
                                        HttpMethod.GET,
                                        "/payment-success.html",
                                        "/payment-failure.html"
                                )
                                .permitAll()

                                // ==========================
                                // Everything else
                                // ==========================
                                .anyRequest()
                                .authenticated()
                )

                .exceptionHandling(exception -> exception

                        .authenticationEntryPoint(
                                jwtAuthenticationEntryPoint
                        )

                        .accessDeniedHandler((request, response, accessDeniedException) -> {

                            log.warn(
                                    "Access denied for {} {}. User={}",
                                    request.getMethod(),
                                    request.getRequestURI(),
                                    request.getUserPrincipal() != null
                                            ? request.getUserPrincipal().getName()
                                            : "anonymous"
                            );

                            response.sendError(
                                    HttpServletResponse.SC_FORBIDDEN,
                                    "Access denied"
                            );
                        })
                )

                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}