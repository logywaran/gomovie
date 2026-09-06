package com.gomovie.seat;

import com.gomovie.user.User;
import com.gomovie.user.UserRepository;
import org.springframework.security.core.Authentication;
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
    private final UserRepository userRepository;

    @PostMapping
    public ResponseEntity<SeatResponse> createSeat(
            @PathVariable Long screenId,
            @Valid @RequestBody SeatRequest request,
            Authentication authentication) {

        User manager = userRepository.findByEmail(
                authentication.getName()
        ).orElseThrow();

        SeatResponse response =
                seatService.createSeat(
                        screenId,
                        request,
                        manager.getId()
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PostMapping("/bulk")
    public ResponseEntity<List<SeatResponse>> createSeats(
            @PathVariable Long screenId,
            @Valid @RequestBody BulkSeatRequest request,
            Authentication authentication) {

        User manager = userRepository.findByEmail(
                authentication.getName()
        ).orElseThrow();

        List<SeatResponse> responses =
                seatService.createSeats(
                        screenId,
                        request,
                        manager.getId()
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(responses);
    }


    @GetMapping
    public ResponseEntity<List<SeatResponse>> getSeatsByScreen(
            @PathVariable Long screenId,
            Authentication authentication) {

        User manager = userRepository.findByEmail(
                authentication.getName()
        ).orElseThrow();

        List<SeatResponse> responses =
                seatService.getSeatsByScreen(
                        screenId,
                        manager.getId()
                );

        return ResponseEntity.ok(responses);
    }
}