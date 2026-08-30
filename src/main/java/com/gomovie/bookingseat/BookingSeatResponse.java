package com.gomovie.bookingseat;

import java.math.BigDecimal;

public record BookingSeatResponse(

        Long showSeatId,

        String rowLabel,

        Integer seatNumber,

        String seatType,

        BigDecimal price

) {
}