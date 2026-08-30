package com.gomovie.common.config;

import com.gomovie.user.Role;
import com.gomovie.user.User;
import com.gomovie.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AdminSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {

        if (userRepository.existsByRole(Role.ADMIN)) {
            log.info("Admin user already exists. Skipping admin creation.");
            return;
        }

        User admin = new User();

        admin.setName("GoMovie Admin");
        admin.setEmail("admin@gomovie.com");

        // Temporary development credentials.
        // Never log the raw password.
        admin.setPassword(
                passwordEncoder.encode("Admin@12345")
        );

        admin.setRole(Role.ADMIN);

        userRepository.save(admin);

        log.info("Initial admin user created successfully.");
    }
}