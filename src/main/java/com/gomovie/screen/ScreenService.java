package com.gomovie.screen;

import java.util.List;

public interface ScreenService {

    ScreenResponse createScreen(
            Long theatreId,
            ScreenRequest request
    );

    List<ScreenResponse> getScreensByTheatre(Long theatreId);

    ScreenResponse getScreenById(Long screenId);
}