package com.gomovie.screen;

public record ScreenResponse(
        Long id,
        Long theatreId,
        String name,
        Boolean isActive
) {
}