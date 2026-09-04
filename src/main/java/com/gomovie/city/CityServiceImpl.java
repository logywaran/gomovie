package com.gomovie.city;

import com.gomovie.common.exception.InvalidStateException;
import com.gomovie.common.exception.ResourceAlreadyExistsException;
import com.gomovie.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CityServiceImpl implements CityService {

    private final CityRepository cityRepository;
    private final CityMapper cityMapper;

    // Create a new city.
    @Override
    public CityResponse create(CityRequest request) {

        String cityName = request.name().trim();

        log.info("Creating city: name={}", cityName);

        // Check whether a city with the same name already exists.
        if (cityRepository.existsByNameIgnoreCase(cityName)) {

            log.warn(
                    "City creation rejected: name={} already exists",
                    cityName
            );

            throw new ResourceAlreadyExistsException(
                    "City already exists with name: " + cityName
            );
        }

        // Create the entity.
        // isActive defaults to true in the City entity.
        City city = new City();
        city.setName(cityName);

        // Save the city to the database.
        cityRepository.save(city);

        log.info(
                "City created successfully: id={}, name={}",
                city.getId(),
                city.getName()
        );

        // Convert the entity into the response DTO.
        return cityMapper.toResponse(city);
    }

    // Return only active cities for customer-facing APIs.
    @Override
    @Transactional(readOnly = true)
    public List<CityResponse> getAllActive() {

        log.debug("Fetching active cities");

        return cityRepository.findByIsActiveTrue()
                .stream()
                .map(cityMapper::toResponse)
                .toList();
    }

    // Return all cities, including inactive cities, for admin management.
    @Override
    @Transactional(readOnly = true)
    public List<CityResponse> getAllForAdmin() {

        log.debug("Fetching all cities for admin");

        return cityRepository.findAll()
                .stream()
                .map(cityMapper::toResponse)
                .toList();
    }

    // Soft-delete a city by marking it inactive.
    @Override
    public void deactivate(Long id) {

        log.info("Deactivating city: id={}", id);

        // Find the city before changing its state.
        City city = cityRepository.findById(id)
                .orElseThrow(() -> {

                    log.warn(
                            "City deactivation failed: id={} not found",
                            id
                    );

                    return new ResourceNotFoundException(
                            "City not found with id: " + id
                    );
                });

        // Prevent deactivating an already inactive city.
        if (!Boolean.TRUE.equals(city.getIsActive())) {

            log.warn(
                    "City deactivation rejected: id={} is already inactive",
                    id
            );

            throw new InvalidStateException(
                    "City is already inactive"
            );
        }

        // Mark the city as inactive.
        city.setIsActive(false);

        // Record when the city was deactivated.
        city.setDeletedAt(LocalDateTime.now());

        // No save() is required.
        // JPA dirty checking detects the changes because the entity
        // is managed inside the current transaction.

        log.info(
                "City deactivated successfully: id={}, name={}",
                city.getId(),
                city.getName()
        );
    }

    // Reactivate a previously deactivated city.
    @Override
    public void reactivate(Long id) {

        log.info("Reactivating city: id={}", id);

        // Find the city before changing its state.
        City city = cityRepository.findById(id)
                .orElseThrow(() -> {

                    log.warn(
                            "City reactivation failed: id={} not found",
                            id
                    );

                    return new ResourceNotFoundException(
                            "City not found with id: " + id
                    );
                });

        // Prevent reactivating an already active city.
        if (Boolean.TRUE.equals(city.getIsActive())) {

            log.warn(
                    "City reactivation rejected: id={} is already active",
                    id
            );

            throw new InvalidStateException(
                    "City is already active"
            );
        }

        // Mark the city as active again.
        city.setIsActive(true);

        // Clear the previous soft-delete timestamp.
        city.setDeletedAt(null);

        log.info(
                "City reactivated successfully: id={}, name={}",
                city.getId(),
                city.getName()
        );
    }
}