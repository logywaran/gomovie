package com.gomovie.screen;

import com.gomovie.user.User;
import com.gomovie.user.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ScreenController {

    private final ScreenService screenService;
    private final UserRepository userRepository;

    @PostMapping("/theatres/{theatreId}/screens")
    public ResponseEntity<ScreenResponse> createScreen(
            @PathVariable Long theatreId,
            @Valid @RequestBody ScreenRequest request,
            Authentication authentication) {

        User manager = userRepository.findByEmail(
                authentication.getName()
        ).orElseThrow();

        ScreenResponse response =
                screenService.createScreen(
                        theatreId,
                        request,
                        manager.getId()
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/theatres/{theatreId}/screens")
    public ResponseEntity<List<ScreenResponse>> getScreensByTheatre(
            @PathVariable Long theatreId,
            Authentication authentication) {

        User manager = userRepository.findByEmail(
                authentication.getName()
        ).orElseThrow();

        List<ScreenResponse> responses =
                screenService.getScreensByTheatre(
                        theatreId,
                        manager.getId()
                );

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/screens/{screenId}")
    public ResponseEntity<ScreenResponse> getScreenById(
            @PathVariable Long screenId,
            Authentication authentication) {

        User manager = userRepository.findByEmail(
                authentication.getName()
        ).orElseThrow();

        ScreenResponse response =
                screenService.getScreenById(
                        screenId,
                        manager.getId()
                );

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/screens/{screenId}")
    public ResponseEntity<ScreenResponse> updateScreen(
            @PathVariable Long screenId,
            @Valid @RequestBody ScreenUpdateRequest request,
            Authentication authentication) {

        User manager = userRepository.findByEmail(
                authentication.getName()
        ).orElseThrow();

        ScreenResponse response =
                screenService.updateScreen(
                        screenId,
                        request,
                        manager.getId()
                );

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/screens/{screenId}/deactivate")
    public ResponseEntity<Void> deactivateScreen(
            @PathVariable Long screenId,
            Authentication authentication) {

        User manager = userRepository.findByEmail(
                authentication.getName()
        ).orElseThrow();

        screenService.deactivateScreen(
                screenId,
                manager.getId()
        );

        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/screens/{screenId}/reactivate")
    public ResponseEntity<Void> reactivateScreen(
            @PathVariable Long screenId,
            Authentication authentication) {

        User manager = userRepository.findByEmail(
                authentication.getName()
        ).orElseThrow();

        screenService.reactivateScreen(
                screenId,
                manager.getId()
        );

        return ResponseEntity.noContent().build();
    }
}