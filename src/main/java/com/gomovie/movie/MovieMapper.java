package com.gomovie.movie;

import org.springframework.stereotype.Component;

@Component
public class MovieMapper {

    public Movie toEntity(MovieRequest request) {

        Movie movie = new Movie();

        movie.setTitle(request.getTitle());
        movie.setDescription(request.getDescription());
        movie.setDurationMinutes(request.getDurationMinutes());
        movie.setReleaseDate(request.getReleaseDate());
        movie.setCertificate(request.getCertificate());
        movie.setPosterUrl(request.getPosterUrl());
        movie.setTrailerUrl(request.getTrailerUrl());

        return movie;
    }

    public MovieResponse  toResponse(Movie movie) {

        MovieResponse response = new MovieResponse();

        response.setId(movie.getId());
        response.setTitle(movie.getTitle());
        response.setDescription(movie.getDescription());
        response.setDurationMinutes(movie.getDurationMinutes());
        response.setReleaseDate(movie.getReleaseDate());
        response.setCertificate(movie.getCertificate());
        response.setPosterUrl(movie.getPosterUrl());
        response.setTrailerUrl(movie.getTrailerUrl());
        response.setIsActive(movie.getIsActive());

        return response;
    }
}