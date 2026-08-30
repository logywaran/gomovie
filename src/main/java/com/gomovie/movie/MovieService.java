package com.gomovie.movie;

public interface MovieService {
    MovieResponse createMovie(MovieRequest request);

    MovieResponse getMovieById(Long id);
}
