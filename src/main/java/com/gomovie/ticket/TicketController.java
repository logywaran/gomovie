package com.gomovie.ticket;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    @PostMapping("/booking/{bookingId}")
    public ResponseEntity<List<TicketResponse>> generateTickets(
            @PathVariable Long bookingId) {

        return ResponseEntity.ok(
                ticketService.generateTickets(bookingId)
        );
    }

    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<List<TicketResponse>> getTicketsByBooking(
            @PathVariable Long bookingId) {

        return ResponseEntity.ok(
                ticketService.getTicketsByBooking(bookingId)
        );
    }
}