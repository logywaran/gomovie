package com.gomovie.movie;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class MovieUpdateRequest {

    @NotBlank
    private String title;

    private String description;

    @NotNull
    @Positive
    private Integer durationMinutes;

    private LocalDate releaseDate;

    private String certificate;

    private String posterUrl;

    private String trailerUrl;
}