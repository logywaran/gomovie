package com.gomovie.seat;

import com.gomovie.user.User;
import com.gomovie.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;


    @RestController
    @RequiredArgsConstructor
    public class SeatLifecycleController {

        private final SeatService seatService;
        private final UserRepository userRepository;

        @PatchMapping("/api/seats/{seatId}/deactivate")
        public ResponseEntity<Void> deactivateSeat(
                @PathVariable Long seatId,
                Authentication authentication) {

            User manager = userRepository.findByEmail(
                    authentication.getName()
            ).orElseThrow();

            seatService.deactivateSeat(
                    seatId,
                    manager.getId()
            );

            return ResponseEntity.noContent().build();
        }

        @PatchMapping("/api/seats/{seatId}/reactivate")
        public ResponseEntity<Void> reactivateSeat(
                @PathVariable Long seatId,
                Authentication authentication) {

            User manager = userRepository.findByEmail(
                    authentication.getName()
            ).orElseThrow();

            seatService.reactivateSeat(
                    seatId,
                    manager.getId()
            );

            return ResponseEntity.noContent().build();
        }
    }

