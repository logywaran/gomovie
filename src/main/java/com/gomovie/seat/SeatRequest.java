package com.gomovie.seat;

import jakarta.validation.constraints.*;

public record SeatRequest(

        @NotBlank(message = "Row label is required")
        @Size(max = 10, message = "Row label must not exceed 10 characters")
        String rowLabel,

        @Min(value = 1, message = "Seat number must be at least 1")
        @Max(value = 999, message = "Seat number must not exceed 999")
        Integer seatNumber,

        @NotNull(message = "Seat type is required")
        SeatType seatType

) {
}