package com.gomovie.seat;

public record SeatResponse(
        Long id,
        Long screenId,
        String rowLabel,
        Integer seatNumber,
        SeatType seatType,
        Boolean isActive
) {
}