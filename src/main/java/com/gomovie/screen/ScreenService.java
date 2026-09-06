package com.gomovie.screen;

import java.util.List;

public interface ScreenService {

    ScreenResponse createScreen(
            Long theatreId,
            ScreenRequest request,
            Long managerId
    );

    List<ScreenResponse> getScreensByTheatre(
            Long theatreId,
            Long managerId
    );

    ScreenResponse getScreenById(
            Long screenId,
            Long managerId
    );

    ScreenResponse updateScreen(
            Long screenId,
            ScreenUpdateRequest request,
            Long managerId
    );

    void deactivateScreen(
            Long screenId,
            Long managerId
    );

    void reactivateScreen(
            Long screenId,
            Long managerId
    );
}