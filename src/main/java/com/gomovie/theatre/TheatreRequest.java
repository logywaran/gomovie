package com.gomovie.theatre;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TheatreRequest(

        @NotNull(message = "City ID is required")
        Long cityId,

        @NotBlank(message = "Theatre name is required")
        @Size(max = 150, message = "Theatre name must not exceed 150 characters")
        String name,

        @NotBlank(message = "Address is required")
        @Size(max = 500, message = "Address must not exceed 500 characters")
        String address
) {
}