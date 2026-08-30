package com.gomovie.show;

import java.time.LocalDate;
import java.time.LocalTime;

public record MovieShowResponse(

        Long id,

        Long movieId,

        Long screenId,

        LocalDate showDate,

        LocalTime startTime,

        LocalTime endTime,

        Boolean isActive

) {
}