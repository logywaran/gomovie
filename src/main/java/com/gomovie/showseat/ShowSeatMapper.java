package com.gomovie.showseat;

import org.springframework.stereotype.Component;

@Component
public class ShowSeatMapper {

    public ShowSeatResponse toResponse(ShowSeat showSeat) {

        return new ShowSeatResponse(
                showSeat.getId(),
                showSeat.getShowId(),
                showSeat.getScreenId(),
                showSeat.getSeatId(),
                showSeat.getSeat().getRowLabel(),
                showSeat.getSeat().getSeatNumber(),
                showSeat.getSeat().getSeatType(),
                showSeat.getStatus(),
                showSeat.getPrice()
        );
    }
}