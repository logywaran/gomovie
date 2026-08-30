package com.gomovie.seat;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record BulkSeatRequest(

        @NotEmpty(message = "Seats cannot be empty")
        @Size(max = 500, message = "Cannot create more than 500 seats at once")
        List<@Valid SeatRequest> seats

) {
}