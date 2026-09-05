package com.gomovie.theatre;

import com.gomovie.city.City;
import com.gomovie.city.CityRepository;
import com.gomovie.common.exception.InvalidStateException;
import com.gomovie.common.exception.ResourceAlreadyExistsException;
import com.gomovie.common.exception.ResourceNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.transaction.annotation.Transactional;
import com.gomovie.user.Role;
import com.gomovie.user.User;
import com.gomovie.user.UserRepository;
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
    private final UserRepository userRepository;

    @Override
    public TheatreResponse create(TheatreRequest request) {

        /*
         * A theatre cannot exist without a valid city.
         *
         * The request only contains cityId, so we resolve the actual
         * City entity before assigning it to the Theatre.
         */
        City city = cityRepository.findById(request.cityId())
                .orElseThrow(() -> {

                    log.warn(
                            "Cannot create theatre '{}'. City not found. cityId={}",
                            request.name(),
                            request.cityId()
                    );

                    return new ResourceNotFoundException(
                            "City not found with id: " + request.cityId()
                    );
                });

        /*
         * The theatre must also have a valid Theatre Manager.
         *
         * We resolve the manager from the User table using managerId.
         */
        User manager = userRepository.findById(request.managerId())
                .orElseThrow(() -> {

                    log.warn(
                            "Cannot create theatre '{}'. Manager not found. managerId={}",
                            request.name(),
                            request.managerId()
                    );

                    return new ResourceNotFoundException(
                            "Theatre manager not found with id: "
                                    + request.managerId()
                    );
                });

        /*
         * A user can only manage a theatre if their role is
         * THEATRE_MANAGER.
         *
         * This prevents accidentally assigning a CUSTOMER or ADMIN
         * as the manager of a theatre.
         */
        if (manager.getRole() != Role.THEATRE_MANAGER) {

            log.warn(
                    "Cannot create theatre '{}'. User {} is not a theatre manager. role={}",
                    request.name(),
                    manager.getId(),
                    manager.getRole()
            );

            throw new InvalidStateException(
                    "User is not a theatre manager"
            );
        }

        log.info(
                "Creating theatre '{}' in city '{}' (cityId={}) with manager '{}' (managerId={})",
                request.name(),
                city.getName(),
                city.getId(),
                manager.getName(),
                manager.getId()
        );

        /*
         * Application-level duplicate check.
         *
         * The database also has a unique constraint on
         * (city_id, name), which acts as the final protection
         * against duplicate theatre names within the same city.
         */
        if (theatreRepository.existsByCityIdAndNameIgnoreCase(
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

        /*
         * The mapper handles simple DTO-to-entity field mapping
         * such as name and address.
         *
         * Relationships such as City and User are resolved by
         * the service because they require repository lookups
         * and business validation.
         */
        Theatre theatre = theatreMapper.toEntity(request);

        // Assign the resolved City entity to the Theatre.
        theatre.setCity(city);

        // Assign the validated Theatre Manager to the Theatre.
        theatre.setManager(manager);

        Theatre savedTheatre = theatreRepository.save(theatre);

        log.info(
                "Theatre created successfully. theatreId={}, name='{}', city='{}' (cityId={}), manager='{}' (managerId={})",
                savedTheatre.getId(),
                savedTheatre.getName(),
                city.getName(),
                city.getId(),
                manager.getName(),
                manager.getId()
        );

        return theatreMapper.toResponse(savedTheatre);
    }

    @Override
    public List<TheatreResponse> getByCity(Long cityId) {

        log.debug("Fetching active theatres for cityId={}", cityId);

        cityRepository.findById(cityId)
                .orElseThrow(() -> {

                    log.warn(
                            "Cannot fetch theatres. City not found. cityId={}",
                            cityId
                    );

                    return new ResourceNotFoundException(
                            "City not found with id: " + cityId
                    );
                });

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
    public List<TheatreResponse> getAllForAdmin() {

        log.info("Fetching all theatres for admin");

        return theatreRepository.findAll()
                .stream()
                .map(theatreMapper::toResponse)
                .toList();
    }

    @Override
    public TheatreResponse getByIdForAdmin(Long theatreId) {

        log.info("Fetching theatre for admin. theatreId={}", theatreId);

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

        /*
         * Unlike the public getById() method,
         * admin is allowed to view inactive theatres.
         */
        log.info(
                "Theatre found for admin. theatreId={}, name='{}', isActive={}",
                theatre.getId(),
                theatre.getName(),
                theatre.getIsActive()
        );

        return theatreMapper.toResponse(theatre);
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

        /*
         * Inactive theatres should not be exposed through
         * customer-facing APIs.
         */
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
    public TheatreResponse updateForAdmin(
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

        /*
         * Prevent modifications to theatres that are no longer active.
         */
        if (!Boolean.TRUE.equals(theatre.getIsActive())) {

            log.warn(
                    "Cannot update theatre. Theatre is inactive. theatreId={}",
                    theatreId
            );

            throw new ResourceNotFoundException(
                    "Theatre not found with id: " + theatreId
            );
        }

        /*
         * Only update the name when a new name was actually provided
         * and it differs from the current name.
         */
        if (request.name() != null
                && !request.name().equals(theatre.getName())) {

            /*
             * The database also has a unique constraint on
             * city + theatre name.
             *
             * This application-level check provides a meaningful
             * business-level error before attempting the database save.
             */
            if (theatreRepository.existsByCityIdAndNameIgnoreCaseAndIdNot(
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

        /*
         * Update the address only when a new address was provided.
         */
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



    @Override
    public List<TheatreResponse> getAllForManager(Long managerId) {

        log.debug(
                "Fetching theatres for managerId={}",
                managerId
        );

        List<Theatre> theatres =
                theatreRepository.findByManagerId(managerId);

        log.debug(
                "Found {} theatres for managerId={}",
                theatres.size(),
                managerId
        );

        return theatres.stream()
                .map(theatreMapper::toResponse)
                .toList();
    }

    @Override
    public TheatreResponse getByIdForManager(
            Long theatreId,
            Long managerId) {

        log.debug(
                "Fetching theatre for manager. theatreId={}, managerId={}",
                theatreId,
                managerId
        );

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

        if (!theatre.getManager().getId().equals(managerId)) {

            log.warn(
                    "Manager does not own theatre. theatreId={}, managerId={}",
                    theatreId,
                    managerId
            );

            throw new AccessDeniedException(
                    "You do not have access to this theatre"
            );
        }

        if (!Boolean.TRUE.equals(theatre.getIsActive())) {

            log.warn(
                    "Manager attempted to access inactive theatre. theatreId={}",
                    theatreId
            );

            throw new InvalidStateException(
                    "Theatre is inactive"
            );
        }

        return theatreMapper.toResponse(theatre);
    }

    @Override
    @Transactional
    public TheatreResponse updateForManager(
            Long theatreId,
            TheatreUpdateRequest request,
            Long managerId) {

        log.debug(
                "Updating theatre for manager. theatreId={}, managerId={}",
                theatreId,
                managerId
        );

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

        // Ownership check
        if (!theatre.getManager().getId().equals(managerId)) {

            log.warn(
                    "Manager does not own theatre. theatreId={}, managerId={}",
                    theatreId,
                    managerId
            );

            throw new AccessDeniedException(
                    "You do not have access to this theatre"
            );
        }

        // Manager cannot modify an inactive theatre
        if (!Boolean.TRUE.equals(theatre.getIsActive())) {

            log.warn(
                    "Manager attempted to update inactive theatre. theatreId={}",
                    theatreId
            );

            throw new InvalidStateException(
                    "Theatre is inactive"
            );
        }

        // Validate duplicate name only if name is being changed
        if (request.name() != null
                && !request.name().equalsIgnoreCase(theatre.getName())
                && theatreRepository.existsByCityIdAndNameIgnoreCaseAndIdNot(
                theatre.getCity().getId(),
                request.name(),
                theatreId
        )) {

            log.warn(
                    "Duplicate theatre name during manager update. cityId={}, name={}",
                    theatre.getCity().getId(),
                    request.name()
            );

            throw new ResourceAlreadyExistsException(
                    "Theatre already exists in city: "
                            + theatre.getCity().getName()
            );
        }

        // Update only fields allowed by TheatreUpdateRequest
        if (request.name() != null) {
            theatre.setName(request.name());
        }

        if (request.address() != null) {
            theatre.setAddress(request.address());
        }

        log.info(
                "Theatre updated successfully by manager. theatreId={}, managerId={}",
                theatreId,
                managerId
        );

        return theatreMapper.toResponse(theatre);
    }

    @Override
    public void deactivate(Long theatreId) {

        log.info("Deactivating theatre. theatreId={}", theatreId);

        Theatre theatre = theatreRepository.findById(theatreId)
                .orElseThrow(() -> {

                    log.warn(
                            "Cannot deactivate theatre. Theatre not found. theatreId={}",
                            theatreId
                    );

                    return new ResourceNotFoundException(
                            "Theatre not found with id: " + theatreId
                    );
                });

        if (!Boolean.TRUE.equals(theatre.getIsActive())) {

            log.warn(
                    "Cannot deactivate theatre. Theatre is already inactive. theatreId={}",
                    theatreId
            );

            throw new InvalidStateException(
                    "Theatre is already inactive"
            );
        }

        theatre.setIsActive(false);
        theatre.setDeletedAt(java.time.LocalDateTime.now());

        theatreRepository.save(theatre);

        log.info(
                "Theatre deactivated successfully. theatreId={}",
                theatreId
        );
    }

    @Override
    public void reactivate(Long theatreId) {

        log.info("Reactivating theatre. theatreId={}", theatreId);

        Theatre theatre = theatreRepository.findById(theatreId)
                .orElseThrow(() -> {

                    log.warn(
                            "Cannot reactivate theatre. Theatre not found. theatreId={}",
                            theatreId
                    );

                    return new ResourceNotFoundException(
                            "Theatre not found with id: " + theatreId
                    );
                });

        if (Boolean.TRUE.equals(theatre.getIsActive())) {

            log.warn(
                    "Cannot reactivate theatre. Theatre is already active. theatreId={}",
                    theatreId
            );

            throw new InvalidStateException(
                    "Theatre is already active"
            );
        }

        theatre.setIsActive(true);
        theatre.setDeletedAt(null);

        theatreRepository.save(theatre);

        log.info(
                "Theatre reactivated successfully. theatreId={}",
                theatreId
        );
    }
}