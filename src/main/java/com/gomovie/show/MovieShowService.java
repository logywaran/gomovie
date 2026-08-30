package com.gomovie.show;

import java.util.List;

public interface MovieShowService {

    MovieShowResponse createShow(
            MovieShowRequest request
    );

    MovieShowResponse getShowById(Long id);

    List<MovieShowResponse> getAllShows();

    MovieShowResponse updateShow(
            Long id,
            MovieShowRequest request
    );

    MovieShowResponse deactivateShow(Long id);
}