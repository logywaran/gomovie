package com.gomovie.city;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cities")
@RequiredArgsConstructor
public class CityController {

    private final CityService cityService;

    @PostMapping
    public ResponseEntity<CityResponse> create(
            @Valid @RequestBody CityRequest request) {

        CityResponse response = cityService.create(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CityResponse> getById(
            @PathVariable Long id) {

        CityResponse response = cityService.getById(id);

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<CityResponse>> getAll() {

        return ResponseEntity.ok(cityService.getAll());
    }
}