package com.gomovie.movie;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/movies")
@RequiredArgsConstructor
public class AdminMovieController {

    private final MovieService movieService;

    @PostMapping
    public ResponseEntity<MovieResponse> createMovie(
            @Valid @RequestBody MovieRequest request) {

        MovieResponse response = movieService.createMovie(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    public ResponseEntity<List<MovieResponse>> getAllMovies() {

        List<MovieResponse> response =
                movieService.getAllMoviesForAdmin();

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MovieResponse> updateMovie(
            @PathVariable Long id,
            @Valid @RequestBody MovieUpdateRequest request) {

        MovieResponse response = movieService.updateMovie(id, request);

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<MovieResponse> deactivateMovie(
            @PathVariable Long id) {

        MovieResponse response = movieService.deactivateMovie(id);

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/reactivate")
    public ResponseEntity<MovieResponse> reactivateMovie(
            @PathVariable Long id) {

        MovieResponse response = movieService.reactivateMovie(id);

        return ResponseEntity.ok(response);
    }
}