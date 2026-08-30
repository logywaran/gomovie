package com.gomovie.seat;

import org.springframework.stereotype.Component;

@Component
public class SeatMapper {

    public Seat toEntity(SeatRequest request) {

        Seat seat = new Seat();

        seat.setRowLabel(request.rowLabel());
        seat.setSeatNumber(request.seatNumber());
        seat.setSeatType(request.seatType());

        return seat;
    }

    public SeatResponse toResponse(Seat seat) {

        return new SeatResponse(
                seat.getId(),
                seat.getScreen().getId(),
                seat.getRowLabel(),
                seat.getSeatNumber(),
                seat.getSeatType(),
                seat.getIsActive()
        );
    }
}