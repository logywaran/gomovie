package com.gomovie.movie;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/movies")
@RequiredArgsConstructor
public class MovieController {

    private final MovieService movieService;

    @GetMapping
    public ResponseEntity<List<MovieResponse>> getMovies(
            @RequestParam(required = false) String certificate,
            @RequestParam(required = false) String sort) {

        List<MovieResponse> response =
                movieService.getMovies(certificate, sort);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MovieResponse> getMovieById(
            @PathVariable Long id) {

        MovieResponse response = movieService.getMovieById(id);

        return ResponseEntity.ok(response);
    }
}