package com.gomovie.theatre;

import java.util.List;

public interface TheatreService {

    TheatreResponse create(TheatreRequest request);

    List<TheatreResponse> getByCity(Long cityId);

    TheatreResponse getById(Long theatreId);

    TheatreResponse updateForAdmin(
            Long theatreId,
            TheatreUpdateRequest request
    );

    List<TheatreResponse> getAllForAdmin();

    TheatreResponse getByIdForAdmin(Long theatreId);

    List<TheatreResponse> getAllForManager(Long managerId);

    TheatreResponse getByIdForManager(
            Long theatreId,
            Long managerId
    );

    TheatreResponse updateForManager(
            Long theatreId,
            TheatreUpdateRequest request,
            Long managerId
    );

    void deactivate(Long theatreId);

    void reactivate(Long theatreId);
}