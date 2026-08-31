package com.gomovie.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final EmailService emailService;

    @PostMapping("/booking/{bookingId}")
    public ResponseEntity<String> sendBookingEmail(
            @PathVariable Long bookingId) {

        emailService.sendBookingConfirmation(bookingId);

        return ResponseEntity.ok(
                "Booking confirmation email sent successfully"
        );
    }
}