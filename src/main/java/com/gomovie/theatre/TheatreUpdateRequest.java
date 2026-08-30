package com.gomovie.theatre;

import jakarta.validation.constraints.Size;

public record TheatreUpdateRequest(

        @Size(max = 150, message = "Theatre name must not exceed 150 characters")
        String name,

        @Size(max = 500, message = "Address must not exceed 500 characters")
        String address
) {
}