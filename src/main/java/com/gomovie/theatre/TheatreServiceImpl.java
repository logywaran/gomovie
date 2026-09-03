package com.gomovie.theatre;

import com.gomovie.city.City;
import com.gomovie.city.CityRepository;
import com.gomovie.common.exception.ResourceAlreadyExistsException;
import com.gomovie.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TheatreServiceImpl implements TheatreService {

    private final TheatreRepository theatreRepository;
    private final CityRepository cityRepository;
    private final TheatreMapper theatreMapper;

    @Override
    public TheatreResponse create(TheatreRequest request) {

        City city = cityRepository.findById(request.cityId())
                .orElseThrow(() -> {

                    // The theatre cannot exist without a valid city.
                    log.warn(
                            "Cannot create theatre '{}'. City not found. cityId={}",
                            request.name(),
                            request.cityId()
                    );

                    return new ResourceNotFoundException(
                            "City not found with id: " + request.cityId()
                    );
                });

        log.info(
                "Creating theatre '{}' in city '{}' (cityId={})",
                request.name(),
                city.getName(),
                city.getId()
        );

        if (theatreRepository.existsByCityIdAndName(
                city.getId(),
                request.name())) {

            log.warn(
                    "Theatre '{}' already exists in city '{}' (cityId={})",
                    request.name(),
                    city.getName(),
                    city.getId()
            );

            throw new ResourceAlreadyExistsException(
                    "Theatre already exists in city: " + city.getName()
            );
        }

        Theatre theatre = theatreMapper.toEntity(request);

        // The mapper only handles DTO-to-entity field mapping.
        // The service resolves the city relationship using the actual City entity.
        theatre.setCity(city);

        Theatre savedTheatre = theatreRepository.save(theatre);

        log.info(
                "Theatre created successfully. theatreId={}, name='{}', city='{}' (cityId={})",
                savedTheatre.getId(),
                savedTheatre.getName(),
                city.getName(),
                city.getId()
        );

        return theatreMapper.toResponse(savedTheatre);
    }

    @Override
    public List<TheatreResponse> getByCity(Long cityId) {

        log.debug("Fetching active theatres for cityId={}", cityId);

        List<Theatre> theatres =
                theatreRepository.findByCityIdAndIsActiveTrue(cityId);

        log.debug(
                "Found {} active theatres for cityId={}",
                theatres.size(),
                cityId
        );

        return theatres.stream()
                .map(theatreMapper::toResponse)
                .toList();
    }

    @Override
    public TheatreResponse getById(Long theatreId) {

        log.debug("Fetching theatre. theatreId={}", theatreId);

        Theatre theatre = theatreRepository.findById(theatreId)
                .orElseThrow(() -> {

                    log.warn(
                            "Theatre not found. theatreId={}",
                            theatreId
                    );

                    return new ResourceNotFoundException(
                            "Theatre not found with id: " + theatreId
                    );
                });

        // Inactive theatres should not be exposed through customer-facing APIs.
        if (!Boolean.TRUE.equals(theatre.getIsActive())) {

            log.warn(
                    "Theatre is inactive. theatreId={}",
                    theatreId
            );

            throw new ResourceNotFoundException(
                    "Theatre not found with id: " + theatreId
            );
        }

        log.debug(
                "Theatre found. theatreId={}, name='{}', city='{}'",
                theatre.getId(),
                theatre.getName(),
                theatre.getCity().getName()
        );

        return theatreMapper.toResponse(theatre);
    }

    @Override
    public TheatreResponse update(
            Long theatreId,
            TheatreUpdateRequest request) {

        log.info("Updating theatre. theatreId={}", theatreId);

        Theatre theatre = theatreRepository.findById(theatreId)
                .orElseThrow(() -> {

                    log.warn(
                            "Cannot update theatre. Theatre not found. theatreId={}",
                            theatreId
                    );

                    return new ResourceNotFoundException(
                            "Theatre not found with id: " + theatreId
                    );
                });

        // Prevent modifications to theatres that are no longer active.
        if (!Boolean.TRUE.equals(theatre.getIsActive())) {

            log.warn(
                    "Cannot update theatre. Theatre is inactive. theatreId={}",
                    theatreId
            );

            throw new ResourceNotFoundException(
                    "Theatre not found with id: " + theatreId
            );
        }

        if (request.name() != null
                && !request.name().equals(theatre.getName())) {

            // The database also has a unique constraint on city + theatre name.
            // This check provides a meaningful business-level error before saving.
            if (theatreRepository.existsByCityIdAndNameAndIdNot(
                    theatre.getCity().getId(),
                    request.name(),
                    theatreId)) {

                log.warn(
                        "Cannot rename theatre. Name '{}' already exists in city '{}' (cityId={})",
                        request.name(),
                        theatre.getCity().getName(),
                        theatre.getCity().getId()
                );

                throw new ResourceAlreadyExistsException(
                        "Theatre already exists in city: "
                                + theatre.getCity().getName()
                );
            }

            log.info(
                    "Renaming theatre. theatreId={}, oldName='{}', newName='{}'",
                    theatreId,
                    theatre.getName(),
                    request.name()
            );

            theatre.setName(request.name());
        }

        if (request.address() != null) {

            log.info(
                    "Updating address. theatreId={}",
                    theatreId
            );

            theatre.setAddress(request.address());
        }

        Theatre updatedTheatre = theatreRepository.save(theatre);

        log.info(
                "Theatre updated successfully. theatreId={}, name='{}', city='{}'",
                updatedTheatre.getId(),
                updatedTheatre.getName(),
                updatedTheatre.getCity().getName()
        );

        return theatreMapper.toResponse(updatedTheatre);
    }
}