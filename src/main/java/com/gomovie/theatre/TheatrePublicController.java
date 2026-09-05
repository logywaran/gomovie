package com.gomovie.theatre;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/theatres")
@RequiredArgsConstructor
public class TheatrePublicController {

    private final TheatreService theatreService;

    @GetMapping("/{theatreId}")
    public ResponseEntity<TheatreResponse> getById(
            @PathVariable Long theatreId) {

        TheatreResponse response =
                theatreService.getById(theatreId);

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<TheatreResponse>> getByCity(
            @RequestParam Long cityId) {

        List<TheatreResponse> theatres =
                theatreService.getByCity(cityId);

        return ResponseEntity.ok(theatres);
    }
}