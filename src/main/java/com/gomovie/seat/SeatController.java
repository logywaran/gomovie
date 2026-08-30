package com.gomovie.seat;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/screens/{screenId}/seats")
@RequiredArgsConstructor
public class SeatController {

    private final SeatService seatService;

    @PostMapping
    public ResponseEntity<SeatResponse> createSeat(
            @PathVariable Long screenId,
            @Valid @RequestBody SeatRequest request) {

        SeatResponse response =
                seatService.createSeat(screenId, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PostMapping("/bulk")
    public ResponseEntity<List<SeatResponse>> createSeats(
            @PathVariable Long screenId,
            @Valid @RequestBody BulkSeatRequest request) {

        List<SeatResponse> responses =
                seatService.createSeats(screenId, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(responses);
    }


    @GetMapping
    public ResponseEntity<List<SeatResponse>> getSeatsByScreen(
            @PathVariable Long screenId) {

        List<SeatResponse> responses =
                seatService.getSeatsByScreen(screenId);

        return ResponseEntity.ok(responses);
    }
}