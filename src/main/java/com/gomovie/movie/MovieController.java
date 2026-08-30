package com.gomovie.movie;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/movies")
@RequiredArgsConstructor
public class MovieController {

    private final MovieService movieService;

    @PostMapping
    public ResponseEntity<MovieResponse> createMovie(
            @Valid @RequestBody MovieRequest request) {

        MovieResponse response = movieService.createMovie(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MovieResponse> getMovieById(
            @PathVariable Long id) {

        MovieResponse response = movieService.getMovieById(id);

        return ResponseEntity.ok(response);
    }
}