package com.gomovie.bookingseat;

import com.gomovie.seat.SeatType;

import java.math.BigDecimal;

public record BookingSeatResponse(

        Long showSeatId,

        String rowLabel,

        Integer seatNumber,

        SeatType seatType,

        BigDecimal price

) {
}