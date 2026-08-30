package com.gomovie.showseat;

import com.gomovie.seat.SeatType;

import java.math.BigDecimal;

public record ShowSeatResponse(

        Long id,

        Long showId,

        Long screenId,

        Long seatId,

        String rowLabel,

        Integer seatNumber,

        SeatType seatType,

        ShowSeatStatus status,

        BigDecimal price

) {
}