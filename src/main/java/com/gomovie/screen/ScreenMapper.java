package com.gomovie.screen;

import org.springframework.stereotype.Component;

@Component
public class ScreenMapper {

    public Screen toEntity(ScreenRequest request) {

        Screen screen = new Screen();

        screen.setName(request.name());

        return screen;
    }

    public ScreenResponse toResponse(Screen screen) {

        return new ScreenResponse(
                screen.getId(),
                screen.getTheatre().getId(),
                screen.getName(),
                screen.getIsActive()
        );
    }
}