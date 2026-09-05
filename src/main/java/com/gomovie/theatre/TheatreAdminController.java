package com.gomovie.theatre;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/theatres")
@RequiredArgsConstructor
public class TheatreAdminController {

    private final TheatreService theatreService;

    @PostMapping
    public ResponseEntity<TheatreResponse> create(
            @Valid @RequestBody TheatreRequest request) {

        TheatreResponse response =
                theatreService.create(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    public ResponseEntity<List<TheatreResponse>> getAll() {

        List<TheatreResponse> theatres =
                theatreService.getAllForAdmin();

        return ResponseEntity.ok(theatres);
    }

    @GetMapping("/{theatreId}")
    public ResponseEntity<TheatreResponse> getById(
            @PathVariable Long theatreId) {

        TheatreResponse response =
                theatreService.getByIdForAdmin(theatreId);

        return ResponseEntity.ok(response);
    }


    @PatchMapping("/{theatreId}")
    public ResponseEntity<TheatreResponse> update(
            @PathVariable Long theatreId,
            @Valid @RequestBody TheatreUpdateRequest request) {

        TheatreResponse response =
                theatreService.updateForAdmin(theatreId, request);

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{theatreId}/deactivate")
    public ResponseEntity<Void> deactivate(
            @PathVariable Long theatreId) {

        theatreService.deactivate(theatreId);

        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{theatreId}/reactivate")
    public ResponseEntity<Void> reactivate(
            @PathVariable Long theatreId) {

        theatreService.reactivate(theatreId);

        return ResponseEntity.noContent().build();
    }


}