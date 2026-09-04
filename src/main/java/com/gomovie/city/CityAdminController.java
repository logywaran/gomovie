package com.gomovie.city;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/cities")
@RequiredArgsConstructor
public class CityAdminController {

    private final CityService cityService;

    // Create a new city.
    @PostMapping
    public ResponseEntity<CityResponse> create(
            @Valid @RequestBody CityRequest request) {

        CityResponse response = cityService.create(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // Return all cities, including inactive cities, for admin management.
    @GetMapping
    public ResponseEntity<List<CityResponse>> getAll() {

        List<CityResponse> responses = cityService.getAllForAdmin();

        return ResponseEntity.ok(
                responses
        );
    }

    // Soft-delete a city by marking it inactive.
    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivate(
            @PathVariable Long id) {

        cityService.deactivate(id);

        return ResponseEntity.noContent().build();
    }

    // Reactivate a previously deactivated city.
    @PatchMapping("/{id}/reactivate")
    public ResponseEntity<Void> reactivate(
            @PathVariable Long id) {

        cityService.reactivate(id);

        return ResponseEntity.noContent().build();
    }
}