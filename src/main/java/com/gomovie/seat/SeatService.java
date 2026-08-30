package com.gomovie.seat;

import java.util.List;

public interface SeatService {

    SeatResponse createSeat(
            Long screenId,
            SeatRequest request
    );

    List<SeatResponse> getSeatsByScreen(Long screenId);

    List<SeatResponse> createSeats(
            Long screenId,
            BulkSeatRequest request
    );

}