package com.gomovie.show;

import java.time.LocalDate;
import java.util.List;

public interface MovieShowService {

    MovieShowResponse createShow(
            MovieShowRequest request,
            Long managerId
    );

    MovieShowResponse getShowById(Long id);

    List<MovieShowResponse> getAllShows();

    List<MovieShowResponse> getShowsForCustomer(
            Long movieId,
            Long theatreId,
            LocalDate showDate
    );

    MovieShowResponse deactivateShow(
            Long id,
            Long managerId
    );

    MovieShowResponse reactivateShow(
            Long id,
            Long managerId
    );
}