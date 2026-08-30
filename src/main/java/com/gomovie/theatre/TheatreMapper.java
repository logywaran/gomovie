package com.gomovie.theatre;

import org.springframework.stereotype.Component;

@Component
public class TheatreMapper {

    public Theatre toEntity(TheatreRequest request) {
        Theatre theatre = new Theatre();

        theatre.setName(request.name());
        theatre.setAddress(request.address());

        return theatre;
    }

    public TheatreResponse toResponse(Theatre theatre) {
        return new TheatreResponse(
                theatre.getId(),
                theatre.getCity().getId(),
                theatre.getName(),
                theatre.getAddress(),
                theatre.getIsActive()
        );
    }
}