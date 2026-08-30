package com.gomovie.user;

import com.gomovie.common.exception.ResourceAlreadyExistsException;
import com.gomovie.common.exception.ResourceNotFoundException;
import com.gomovie.common.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Override
    public UserResponse register(UserRequest request) {

        log.info("Registering new user with email: {}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResourceAlreadyExistsException(
                    "User already exists with email: " + request.getEmail()
            );
        }

        User user = new User();

        user.setName(request.getName());
        user.setEmail(request.getEmail());

        // Store only the encoded password. Never store or log the raw password.
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        // Normal self-registration creates a customer account.
        user.setRole(Role.CUSTOMER);

        User savedUser = userRepository.save(user);

        log.info("User registered successfully with id: {}", savedUser.getId());

        return userMapper.toResponse(savedUser);
    }

    @Override
    public LoginResponse login(LoginRequest request) {

        log.info("Login attempt for user with email: {}", request.getEmail());

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with email: " + request.getEmail()
                        )
                );

        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPassword())) {

            log.warn("Login failed for user with email: {}", request.getEmail());

            throw new RuntimeException("Invalid email or password");
        }

        String token = jwtService.generateToken(user);

        log.info("User logged in successfully with id: {}", user.getId());

        return new LoginResponse(token);
    }
}