package com.gomovie.city;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CityRequest(

        @NotBlank(message = "City name is required")
        @Size(min = 2, max = 100, message = "City name must be between 2 and 100 characters")
        String name

) {
}