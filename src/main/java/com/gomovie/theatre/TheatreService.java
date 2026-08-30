package com.gomovie.theatre;

import java.util.List;

public interface TheatreService {

    TheatreResponse create(TheatreRequest request);

    List<TheatreResponse> getByCity(Long cityId);

    TheatreResponse getById(Long theatreId);

    TheatreResponse update(Long theatreId, TheatreUpdateRequest request);
}