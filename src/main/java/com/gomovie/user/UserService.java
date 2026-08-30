package com.gomovie.user;

public interface UserService {

    UserResponse register(UserRequest request);

    LoginResponse login(LoginRequest request);
}