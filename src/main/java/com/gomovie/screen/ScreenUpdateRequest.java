package com.gomovie.screen;

import jakarta.validation.constraints.Size;

public record ScreenUpdateRequest(

        @Size(
                max = 100,
                message = "Screen name must not exceed 100 characters"
        )
        String name

) {
}