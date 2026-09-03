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

            log.warn("City already exists with name: {}", request.name());

            throw new ResourceAlreadyExistsException(
                    "City already exists with name: " + request.name()
            );
        }

        City city = new City();
        city.setName(request.name());

        cityRepository.save(city);

        log.info(
                "City '{}' created successfully with id: {}",
                city.getName(),
                city.getId()
        );

        return new CityResponse(
                city.getId(),
                city.getName()
        );
    }

    @Override
    public CityResponse getById(Long id) {

        log.debug("Fetching city with id: {}", id);

        City city = cityRepository.findById(id)
                .orElseThrow(() -> {

                    log.warn("City not found with id: {}", id);

                    return new ResourceNotFoundException(
                            "City not found with id: " + id
                    );
                });

        return new CityResponse(
                city.getId(),
                city.getName()
        );
    }

    @Override
    public List<CityResponse> getAll() {

        log.debug("Fetching all active cities");

        return cityRepository.findByIsActiveTrue()
                .stream()
                .map(city -> new CityResponse(
                        city.getId(),
                        city.getName()
                ))
                .toList();
    }
}