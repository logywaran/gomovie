package com.gomovie.city;

import java.util.List;

public interface CityService {

    CityResponse create(CityRequest request);

    List<CityResponse> getAllActive();

    List<CityResponse> getAllForAdmin();

    void deactivate(Long id);

    void reactivate(Long id);
}