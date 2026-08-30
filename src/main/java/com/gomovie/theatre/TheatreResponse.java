package com.gomovie.theatre;

public record TheatreResponse(
        Long id,
        Long cityId,
        String name,
        String address,
        Boolean isActive
) {
}