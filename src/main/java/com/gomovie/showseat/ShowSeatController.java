package com.gomovie.showseat;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/show-seats")
@RequiredArgsConstructor
public class ShowSeatController {

    private final ShowSeatService showSeatService;

    @GetMapping("/show/{showId}")
    public ResponseEntity<List<ShowSeatResponse>> getShowSeats(
            @PathVariable Long showId
    ) {
        return ResponseEntity.ok(
                showSeatService.getShowSeats(showId)
        );
    }
}