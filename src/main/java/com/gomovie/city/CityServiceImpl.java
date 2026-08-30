package com.gomovie.city;

import com.gomovie.common.exception.ResourceAlreadyExistsException;
import com.gomovie.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CityServiceImpl implements CityService {

    private final CityRepository cityRepository;

    @Override
    public CityResponse create(CityRequest request) {

        log.info("Creating city with name: {}", request.name());

        if (cityRepository.existsByName(request.name())) {
            throw new ResourceAlreadyExistsException(
                    "City already exists with name: " + request.name()
            );
        }

        City city = new City();
        city.setName(request.name());

        cityRepository.save(city);

        log.info("City created successfully with id: {}", city.getId());

        return new CityResponse(
                city.getId(),
                city.getName()
        );
    }

    @Override
    public CityResponse getById(Long id) {

        log.info("Fetching city with id: {}", id);

        City city = cityRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "City not found with id: " + id
                        )
                );

        return new CityResponse(
                city.getId(),
                city.getName()
        );
    }

    @Override
    public List<CityResponse> getAll() {

        log.info("Fetching all active cities");

        return cityRepository.findAll()
                .stream()
                .filter(city -> Boolean.TRUE.equals(city.getIsActive()))
                .map(city -> new CityResponse(
                        city.getId(),
                        city.getName()
                ))
                .toList();
    }
}