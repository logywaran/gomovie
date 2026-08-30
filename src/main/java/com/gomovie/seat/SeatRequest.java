package com.gomovie.seat;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SeatRequest(

        @NotBlank(message = "Row label is required")
        @Size(max = 10, message = "Row label must not exceed 10 characters")
        String rowLabel,

        @Min(value = 1, message = "Seat number must be at least 1")
        @Max(value = 999, message = "Seat number must not exceed 999")
        Integer seatNumber,

        @NotBlank(message = "Seat type is required")
        @Size(max = 50, message = "Seat type must not exceed 50 characters")
        String seatType

) {
}