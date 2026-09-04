package com.gomovie.city;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cities")
@RequiredArgsConstructor
public class CityController {

    private final CityService cityService;

    // Return all active cities for customer city selection.
    @GetMapping
    public ResponseEntity<List<CityResponse>> getActiveCities() {

        List<CityResponse> responses = cityService.getAllActive();

        return ResponseEntity.ok(
                responses
        );
    }
}