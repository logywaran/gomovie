package com.gomovie.theatre;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/theatres")
@RequiredArgsConstructor
public class TheatreController {

    private final TheatreService theatreService;

    @PostMapping
    public ResponseEntity<TheatreResponse> create(
            @Valid @RequestBody TheatreRequest request) {

        TheatreResponse response = theatreService.create(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/{theatreId}")
    public ResponseEntity<TheatreResponse> getById(
            @PathVariable Long theatreId) {

        TheatreResponse response = theatreService.getById(theatreId);

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<TheatreResponse>> getByCity(
            @RequestParam Long cityId) {

        List<TheatreResponse> theatres =
                theatreService.getByCity(cityId);

        return ResponseEntity.ok(theatres);
    }

    @PatchMapping("/{theatreId}")
    public ResponseEntity<TheatreResponse> update(
            @PathVariable Long theatreId,
            @Valid @RequestBody TheatreUpdateRequest request) {

        TheatreResponse response =
                theatreService.update(theatreId, request);

        return ResponseEntity.ok(response);
    }
}