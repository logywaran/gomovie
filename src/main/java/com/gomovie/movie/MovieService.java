package com.gomovie.movie;

import java.util.List;

public interface MovieService {

    MovieResponse createMovie(MovieRequest request);

    List<MovieResponse> getMovies(String certificate, String sort);

    MovieResponse getMovieById(Long id);

    MovieResponse updateMovie(Long id, MovieUpdateRequest request);

    MovieResponse deactivateMovie(Long id);

    MovieResponse reactivateMovie(Long id);

    List<MovieResponse> getAllMoviesForAdmin();
}