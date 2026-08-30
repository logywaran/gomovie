package com.gomovie.city;

import java.util.List;

public interface CityService {

    CityResponse create(CityRequest request);

    CityResponse getById(Long id);

    List<CityResponse> getAll();
}