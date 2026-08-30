package com.gomovie.booking;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record BookingRequest(

        @NotNull(message = "Show ID is required")
        Long showId,

        @NotEmpty(message = "At least one seat must be selected")
        List<Long> showSeatIds
) {
}