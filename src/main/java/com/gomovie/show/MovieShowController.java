package com.gomovie.show;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/shows")
@RequiredArgsConstructor
public class MovieShowController {

    private final MovieShowService movieShowService;

    @PostMapping
    public ResponseEntity<MovieShowResponse> createShow(
            @Valid @RequestBody MovieShowRequest request) {

        MovieShowResponse response =
                movieShowService.createShow(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MovieShowResponse> getShowById(
            @PathVariable Long id) {

        MovieShowResponse response =
                movieShowService.getShowById(id);

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<MovieShowResponse>> getAllShows() {

        List<MovieShowResponse> response =
                movieShowService.getAllShows();

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MovieShowResponse> updateShow(
            @PathVariable Long id,
            @Valid @RequestBody MovieShowRequest request) {

        MovieShowResponse response =
                movieShowService.updateShow(id, request);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<MovieShowResponse> deactivateShow(
            @PathVariable Long id) {

        MovieShowResponse response =
                movieShowService.deactivateShow(id);

        return ResponseEntity.ok(response);
    }
}