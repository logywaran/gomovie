package com.gomovie.screen;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ScreenRequest(

        @NotBlank(message = "Screen name is required")
        @Size(max = 100, message = "Screen name must not exceed 100 characters")
        String name

) {
}