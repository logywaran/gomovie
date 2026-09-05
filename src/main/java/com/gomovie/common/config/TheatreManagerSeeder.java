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
public class TheatreManagerSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {

        if (!userRepository.existsByRole(Role.THEATRE_MANAGER)) {

            User theatreManager = new User();

            theatreManager.setName("GoMovie Theatre Manager");
            theatreManager.setEmail("manager@gomovie.com");

            theatreManager.setPassword(
                    passwordEncoder.encode("Manager@12345")
            );

            theatreManager.setRole(Role.THEATRE_MANAGER);

            userRepository.save(theatreManager);

            log.info("Initial theatre manager user created successfully.");
        }

        if (userRepository.findByEmail("manager2@gomovie.com").isEmpty()) {

            User theatreManager2 = new User();

            theatreManager2.setName("GoMovie Theatre Manager 2");
            theatreManager2.setEmail("manager2@gomovie.com");

            theatreManager2.setPassword(
                    passwordEncoder.encode("Manager2@12345")
            );

            theatreManager2.setRole(Role.THEATRE_MANAGER);

            userRepository.save(theatreManager2);

            log.info("Second theatre manager user created successfully.");
        }
    }
}