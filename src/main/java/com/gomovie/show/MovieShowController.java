package com.gomovie.show;

import com.gomovie.user.User;
import com.gomovie.user.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/shows")
@RequiredArgsConstructor
public class MovieShowController {

    private final MovieShowService movieShowService;
    private final UserRepository userRepository;

    @PostMapping
    public ResponseEntity<MovieShowResponse> createShow(
            @Valid @RequestBody MovieShowRequest request,
            Authentication authentication) {

        User manager = userRepository.findByEmail(
                authentication.getName()
        ).orElseThrow();

        MovieShowResponse response =
                movieShowService.createShow(
                        request,
                        manager.getId()
                );

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

    @GetMapping("/movie/{movieId}/theatre/{theatreId}")
    public ResponseEntity<List<MovieShowResponse>> getShowsForCustomer(
            @PathVariable Long movieId,
            @PathVariable Long theatreId,
            @RequestParam LocalDate date) {

        List<MovieShowResponse> response =
                movieShowService.getShowsForCustomer(
                        movieId,
                        theatreId,
                        date
                );

        return ResponseEntity.ok(response);
    }


    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<MovieShowResponse> deactivateShow(
            @PathVariable Long id,
            Authentication authentication) {

        User manager = userRepository.findByEmail(
                authentication.getName()
        ).orElseThrow();

        MovieShowResponse response =
                movieShowService.deactivateShow(
                        id,
                        manager.getId()
                );

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/reactivate")
    public ResponseEntity<MovieShowResponse> reactivateShow(
            @PathVariable Long id,
            Authentication authentication) {

        User manager = userRepository.findByEmail(
                authentication.getName()
        ).orElseThrow();

        MovieShowResponse response =
                movieShowService.reactivateShow(
                        id,
                        manager.getId()
                );

        return ResponseEntity.ok(response);
    }
}