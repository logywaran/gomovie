package com.gomovie.screen;

import com.gomovie.common.exception.ResourceAlreadyExistsException;
import com.gomovie.common.exception.ResourceNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import com.gomovie.common.exception.InvalidStateException;
import com.gomovie.theatre.Theatre;
import com.gomovie.theatre.TheatreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScreenServiceImpl implements ScreenService {

    private final ScreenRepository screenRepository;
    private final TheatreRepository theatreRepository;
    private final ScreenMapper screenMapper;

    @Override
    public ScreenResponse createScreen(
            Long theatreId,
            ScreenRequest request,
            Long managerId) {

        Theatre theatre = theatreRepository.findById(theatreId)
                .orElseThrow(() -> {
                    log.warn(
                            "Cannot create screen '{}'. Theatre not found. theatreId={}",
                            request.name(),
                            theatreId
                    );

                    return new ResourceNotFoundException(
                            "Theatre not found with id: " + theatreId
                    );
                });

        if (!Boolean.TRUE.equals(theatre.getIsActive())) {

            log.warn(
                    "Cannot create screen '{}'. Theatre is inactive. theatreId={}",
                    request.name(),
                    theatreId
            );

            throw new ResourceNotFoundException(
                    "Theatre not found with id: " + theatreId
            );
        }

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

        if (screenRepository.existsByTheatreIdAndNameIgnoreCase(
                theatreId,
                request.name())) {

            log.warn(
                    "Screen '{}' already exists in theatre '{}' (theatreId={})",
                    request.name(),
                    theatre.getName(),
                    theatreId
            );

            throw new ResourceAlreadyExistsException(
                    "Screen already exists in this theatre"
            );
        }

        Screen screen = screenMapper.toEntity(request);

        screen.setTheatre(theatre);

        Screen savedScreen = screenRepository.save(screen);

        log.info(
                "Screen created successfully. screenId={}, name='{}', theatre='{}' (theatreId={})",
                savedScreen.getId(),
                savedScreen.getName(),
                theatre.getName(),
                theatreId
        );

        return screenMapper.toResponse(savedScreen);
    }

    @Override
    public List<ScreenResponse> getScreensByTheatre(Long theatreId,Long managerId) {

        log.info(
                "Fetching screens for theatreId={}",
                theatreId
        );

        Theatre theatre = theatreRepository.findById(theatreId)
                .orElseThrow(() -> {
                    log.warn(
                            "Cannot fetch screens. Theatre not found. theatreId={}",
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
                    "Cannot fetch screens. Theatre is inactive. theatreId={}",
                    theatreId
            );

            throw new ResourceNotFoundException(
                    "Theatre not found with id: " + theatreId
            );
        }



        return screenRepository
                .findAllByTheatreId(theatreId)
                .stream()
                .map(screenMapper::toResponse)
                .toList();
    }

    @Override
    public ScreenResponse getScreenById(Long screenId,Long managerId) {

        log.info(
                "Fetching screen. screenId={}",
                screenId
        );

        Screen screen = screenRepository.findById(screenId)
                .orElseThrow(() -> {
                    log.warn(
                            "Cannot fetch screen. Screen not found. screenId={}",
                            screenId
                    );

                    return new ResourceNotFoundException(
                            "Screen not found with id: " + screenId
                    );
                });

        if (!screen.getTheatre().getManager().getId().equals(managerId)) {

            log.warn(
                    "Manager does not own screen. screenId={}, managerId={}",
                    screenId,
                    managerId
            );

            throw new AccessDeniedException(
                    "You do not have access to this screen"
            );
        }

        if (!Boolean.TRUE.equals(screen.getIsActive())) {

            log.warn(
                    "Cannot fetch screen. Screen is inactive. screenId={}",
                    screenId
            );

            throw new ResourceNotFoundException(
                    "Screen not found with id: " + screenId
            );
        }



        return screenMapper.toResponse(screen);
    }

    @Override
    public ScreenResponse updateScreen(
            Long screenId,
            ScreenUpdateRequest request,
            Long managerId) {

        log.info(
                "Updating screen. screenId={}, managerId={}",
                screenId,
                managerId
        );

        Screen screen = screenRepository.findById(screenId)
                .orElseThrow(() -> {

                    log.warn(
                            "Cannot update screen. Screen not found. screenId={}",
                            screenId
                    );

                    return new ResourceNotFoundException(
                            "Screen not found with id: " + screenId
                    );
                });

        /*
         * A theatre manager can modify a screen only when
         * the screen belongs to one of their theatres.
         */
        if (!screen.getTheatre().getManager().getId().equals(managerId)) {

            log.warn(
                    "Manager does not own screen. screenId={}, managerId={}",
                    screenId,
                    managerId
            );

            throw new AccessDeniedException(
                    "You do not have access to this screen"
            );
        }

        /*
         * Inactive screens cannot be modified.
         */
        if (!Boolean.TRUE.equals(screen.getIsActive())) {

            log.warn(
                    "Cannot update screen. Screen is inactive. screenId={}",
                    screenId
            );

            throw new ResourceNotFoundException(
                    "Screen not found with id: " + screenId
            );
        }

        /*
         * Only check for duplicates when the screen name
         * is actually being changed.
         */
        if (request.name() != null
                && !request.name().equalsIgnoreCase(screen.getName())
                && screenRepository.existsByTheatreIdAndNameIgnoreCaseAndIdNot(
                screen.getTheatre().getId(),
                request.name(),
                screenId
        )) {

            log.warn(
                    "Cannot rename screen. Name '{}' already exists in theatre. screenId={}, theatreId={}",
                    request.name(),
                    screenId,
                    screen.getTheatre().getId()
            );

            throw new ResourceAlreadyExistsException(
                    "Screen already exists in this theatre"
            );
        }

        if (request.name() != null) {

            log.info(
                    "Renaming screen. screenId={}, oldName='{}', newName='{}'",
                    screenId,
                    screen.getName(),
                    request.name()
            );

            screen.setName(request.name());
        }

        Screen updatedScreen = screenRepository.save(screen);

        log.info(
                "Screen updated successfully. screenId={}, name='{}'",
                updatedScreen.getId(),
                updatedScreen.getName()
        );

        return screenMapper.toResponse(updatedScreen);
    }

    @Override
    public void deactivateScreen(
            Long screenId,
            Long managerId) {

        log.info(
                "Deactivating screen. screenId={}, managerId={}",
                screenId,
                managerId
        );

        Screen screen = screenRepository.findById(screenId)
                .orElseThrow(() -> {

                    log.warn(
                            "Cannot deactivate screen. Screen not found. screenId={}",
                            screenId
                    );

                    return new ResourceNotFoundException(
                            "Screen not found with id: " + screenId
                    );
                });

        /*
         * A theatre manager can deactivate a screen only when
         * the screen belongs to one of their theatres.
         */
        if (!screen.getTheatre().getManager().getId().equals(managerId)) {

            log.warn(
                    "Manager does not own screen. screenId={}, managerId={}",
                    screenId,
                    managerId
            );

            throw new AccessDeniedException(
                    "You do not have access to this screen"
            );
        }

        /*
         * Prevent deactivating an already inactive screen.
         */
        if (!Boolean.TRUE.equals(screen.getIsActive())) {

            log.warn(
                    "Cannot deactivate screen. Screen is already inactive. screenId={}",
                    screenId
            );

            throw new InvalidStateException(
                    "Screen is already inactive"
            );
        }

        screen.setIsActive(false);
        screen.setDeletedAt(java.time.LocalDateTime.now());

        screenRepository.save(screen);

        log.info(
                "Screen deactivated successfully. screenId={}",
                screenId
        );
    }

    @Override
    public void reactivateScreen(
            Long screenId,
            Long managerId) {

        log.info(
                "Reactivating screen. screenId={}, managerId={}",
                screenId,
                managerId
        );

        Screen screen = screenRepository.findById(screenId)
                .orElseThrow(() -> {

                    log.warn(
                            "Cannot reactivate screen. Screen not found. screenId={}",
                            screenId
                    );

                    return new ResourceNotFoundException(
                            "Screen not found with id: " + screenId
                    );
                });

        /*
         * A theatre manager can reactivate a screen only when
         * the screen belongs to one of their theatres.
         */
        if (!screen.getTheatre().getManager().getId().equals(managerId)) {

            log.warn(
                    "Manager does not own screen. screenId={}, managerId={}",
                    screenId,
                    managerId
            );

            throw new AccessDeniedException(
                    "You do not have access to this screen"
            );
        }

        /*
         * Prevent reactivating an already active screen.
         */
        if (Boolean.TRUE.equals(screen.getIsActive())) {

            log.warn(
                    "Cannot reactivate screen. Screen is already active. screenId={}",
                    screenId
            );

            throw new InvalidStateException(
                    "Screen is already active"
            );
        }

        screen.setIsActive(true);
        screen.setDeletedAt(null);

        screenRepository.save(screen);

        log.info(
                "Screen reactivated successfully. screenId={}",
                screenId
        );
    }


}