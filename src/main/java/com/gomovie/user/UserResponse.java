package com.gomovie.user;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserResponse {

    private Long id;
    private String name;
    private String email;
    private Role role;
}