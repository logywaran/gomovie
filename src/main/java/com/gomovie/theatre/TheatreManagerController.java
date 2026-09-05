package com.gomovie.theatre;

import com.gomovie.common.exception.ResourceNotFoundException;
import com.gomovie.user.User;
import com.gomovie.user.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/manager/theatres")
@RequiredArgsConstructor
public class TheatreManagerController {

    private final TheatreService theatreService;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<List<TheatreResponse>> getMyTheatres() {

        String email =
                SecurityContextHolder.getContext()
                        .getAuthentication()
                        .getName();

        User manager = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found"
                        )
                );

        List<TheatreResponse> theatres =
                theatreService.getAllForManager(manager.getId());

        return ResponseEntity.ok(theatres);
    }

    @GetMapping("/{theatreId}")
    public ResponseEntity<TheatreResponse> getById(
            @PathVariable Long theatreId) {

        String email =
                SecurityContextHolder.getContext()
                        .getAuthentication()
                        .getName();

        User manager = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found"
                        ));

        TheatreResponse response =
                theatreService.getByIdForManager(
                        theatreId,
                        manager.getId()
                );

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{theatreId}")
    public ResponseEntity<TheatreResponse> update(
            @PathVariable Long theatreId,
            @Valid @RequestBody TheatreUpdateRequest request) {

        String email =
                SecurityContextHolder.getContext()
                        .getAuthentication()
                        .getName();

        User manager = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found"
                        ));

        TheatreResponse response =
                theatreService.updateForManager(
                        theatreId,
                        request,
                        manager.getId()
                );

        return ResponseEntity.ok(response);
    }
}