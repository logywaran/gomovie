package com.gomovie.screen;

import com.gomovie.common.exception.ResourceAlreadyExistsException;
import com.gomovie.common.exception.ResourceNotFoundException;
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
            ScreenRequest request) {

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

        if (screenRepository.existsByTheatreIdAndName(
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
    public List<ScreenResponse> getScreensByTheatre(Long theatreId) {

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
                .findAllByTheatreIdAndIsActiveTrue(theatreId)
                .stream()
                .map(screenMapper::toResponse)
                .toList();
    }

    @Override
    public ScreenResponse getScreenById(Long screenId) {

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


}