package com.gomovie.showseat;

import java.math.BigDecimal;

public record ShowSeatResponse(

        Long id,

        Long showId,

        Long screenId,

        Long seatId,

        String rowLabel,

        Integer seatNumber,

        String seatType,

        ShowSeatStatus status,

        BigDecimal price

) {
}