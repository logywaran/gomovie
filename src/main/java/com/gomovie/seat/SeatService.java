package com.gomovie.seat;

import java.util.List;

public interface SeatService {

    SeatResponse createSeat(
            Long screenId,
            SeatRequest request,
            Long managerId
    );

    List<SeatResponse> getSeatsByScreen(
            Long screenId,
            Long managerId
    );

    List<SeatResponse> createSeats(
            Long screenId,
            BulkSeatRequest request,
            Long managerId
    );

    void deactivateSeat(
            Long seatId,
            Long managerId
    );

    void reactivateSeat(
            Long seatId,
            Long managerId
    );

}