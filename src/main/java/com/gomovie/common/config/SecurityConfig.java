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

                // ==========================
                // Security Configuration
                // ==========================
                .csrf(csrf -> csrf.disable())

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                // ==========================
                // Authorization
                // ==========================
                .authorizeHttpRequests(auth -> auth

                        // ---------- Public Authentication ----------
                        .requestMatchers(
                                "/api/users/register",
                                "/api/users/login"
                        )
                        .permitAll()

                        // ---------- Public APIs ----------
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/cities",
                                "/api/movies/**",
                                "/api/shows/**",
                                "/api/theatres/**",
                                "/api/screens/**"
                        )
                        .permitAll()

                        // ---------- API Documentation ----------
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/v3/api-docs/**"
                        )
                        .permitAll()

                        // ---------- Admin APIs ----------
                        .requestMatchers(
                                "/api/admin/cities/**",
                                "/api/admin/movies/**"
                        )
                        .hasRole("ADMIN")

                        // ---------- Theatre Manager APIs ----------
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

                        // ---------- Customer APIs ----------
                        .requestMatchers(
                                "/api/bookings/**"
                        )
                        .hasRole("CUSTOMER")

                        // ---------- Mobi Payment APIs ----------
                        .requestMatchers(
                                "/api/payments/mobi/deposit",
                                "/api/payments/mobi/redirect"
                        )
                        .permitAll()

                        // ---------- Payment Result Pages ----------
                        .requestMatchers(
                                HttpMethod.GET,
                                "/payment-success.html",
                                "/payment-failure.html"
                        )
                        .permitAll()

                        // ---------- All Other Requests ----------
                        .anyRequest()
                        .authenticated()
                )

                // ==========================
                // Exception Handling
                // ==========================
                .exceptionHandling(exception -> exception

                        // Unauthenticated request → 401
                        .authenticationEntryPoint(
                                jwtAuthenticationEntryPoint
                        )

                        // Authenticated but insufficient permission → 403
                        .accessDeniedHandler((request, response, accessDeniedException) -> {

                            log.warn(
                                    "Access denied: method={}, uri={}, user={}",
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

                // ==========================
                // JWT Authentication Filter
                // ==========================
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}