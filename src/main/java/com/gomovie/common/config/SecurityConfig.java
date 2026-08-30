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
import org.springframework.security.core.context.SecurityContextHolder;
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
                        .requestMatchers(
                                "/api/users/register",
                                "/api/users/login",
                                "/api/movies/**",
                                "/api/shows/**",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/error"
                        ).permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/cities").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/cities/**").permitAll()
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/cities"
                        ).hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/theatres/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/theatres/*/screens")
                        .hasAnyRole("THEATRE_MANAGER", "ADMIN")
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/screens/**"
                        ).permitAll()
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/screens/*/seats"
                        ).hasAnyRole("THEATRE_MANAGER", "ADMIN")


                        .anyRequest().authenticated()
                )

                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(
                                jwtAuthenticationEntryPoint
                        )
                        .accessDeniedHandler((request, response, accessDeniedException) -> {

                            log.warn(
                                    "Access denied for {} {}. User={}, Authorities={}, Reason={}",
                                    request.getMethod(),
                                    request.getRequestURI(),
                                    request.getUserPrincipal() != null
                                            ? request.getUserPrincipal().getName()
                                            : "anonymous",
                                    SecurityContextHolder.getContext()
                                            .getAuthentication() != null
                                            ? SecurityContextHolder.getContext()
                                              .getAuthentication()
                                              .getAuthorities()
                                            : "none",
                                    accessDeniedException.getMessage()
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