package com.gomovie.showseat;

import java.util.List;

public interface ShowSeatService {

    List<ShowSeatResponse> generateShowSeats(Long showId);

    List<ShowSeatResponse> getShowSeats(Long showId);
}