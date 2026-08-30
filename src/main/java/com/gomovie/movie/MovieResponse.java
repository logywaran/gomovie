package com.gomovie.movie;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class MovieResponse {

    private Long id;
    private String title;
    private String description;
    private Integer durationMinutes;
    private LocalDate releaseDate;
    private String certificate;
    private String posterUrl;
    private String trailerUrl;
    private Boolean isActive;
}