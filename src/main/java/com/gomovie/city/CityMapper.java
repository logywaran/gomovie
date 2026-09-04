package com.gomovie.city;

import org.springframework.stereotype.Component;

@Component
public class CityMapper {

    public CityResponse toResponse(City city) {
        return new CityResponse(
                city.getId(),
                city.getName()
        );
    }
}