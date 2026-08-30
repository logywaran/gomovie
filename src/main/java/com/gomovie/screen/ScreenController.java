package com.gomovie.screen;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ScreenController {

    private final ScreenService screenService;

    @PostMapping("/theatres/{theatreId}/screens")
    public ResponseEntity<ScreenResponse> createScreen(
            @PathVariable Long theatreId,
            @Valid @RequestBody ScreenRequest request) {

        ScreenResponse response =
                screenService.createScreen(theatreId, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/theatres/{theatreId}/screens")
    public ResponseEntity<List<ScreenResponse>> getScreensByTheatre(
            @PathVariable Long theatreId) {

        List<ScreenResponse> responses =
                screenService.getScreensByTheatre(theatreId);

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/screens/{screenId}")
    public ResponseEntity<ScreenResponse> getScreenById(
            @PathVariable Long screenId) {

        ScreenResponse response =
                screenService.getScreenById(screenId);

        return ResponseEntity.ok(response);
    }
}