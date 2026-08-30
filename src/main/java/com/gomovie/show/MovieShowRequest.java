package com.gomovie.show;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

public record MovieShowRequest(

        @NotNull(message = "Movie ID is required")
        Long movieId,

        @NotNull(message = "Screen ID is required")
        Long screenId,

        @NotNull(message = "Show date is required")
        @FutureOrPresent(message = "Show date cannot be in the past")
        LocalDate showDate,

        @NotNull(message = "Start time is required")
        LocalTime startTime,

        @NotNull(message = "End time is required")
        LocalTime endTime

) {
}