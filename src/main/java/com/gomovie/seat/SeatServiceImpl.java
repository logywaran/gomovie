package com.gomovie.seat;

import com.gomovie.common.exception.ResourceAlreadyExistsException;
import com.gomovie.common.exception.ResourceNotFoundException;
import com.gomovie.screen.Screen;
import com.gomovie.screen.ScreenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class SeatServiceImpl implements SeatService {

    private final SeatRepository seatRepository;
    private final ScreenRepository screenRepository;
    private final SeatMapper seatMapper;

    @Override
    public SeatResponse createSeat(
            Long screenId,
            SeatRequest request) {

        log.info(
                "Creating seat '{}{}' of type '{}' for screenId={}",
                request.rowLabel(),
                request.seatNumber(),
                request.seatType(),
                screenId
        );

        Screen screen = screenRepository.findById(screenId)
                .orElseThrow(() -> {
                    log.warn(
                            "Cannot create seat '{}{}'. Screen not found. screenId={}",
                            request.rowLabel(),
                            request.seatNumber(),
                            screenId
                    );

                    return new ResourceNotFoundException(
                            "Screen not found with id: " + screenId
                    );
                });

        if (!Boolean.TRUE.equals(screen.getIsActive())) {

            log.warn(
                    "Cannot create seat '{}{}'. Screen is inactive. screenId={}",
                    request.rowLabel(),
                    request.seatNumber(),
                    screenId
            );

            throw new ResourceNotFoundException(
                    "Screen not found with id: " + screenId
            );
        }

        if (seatRepository.existsByScreenIdAndRowLabelAndSeatNumber(
                screenId,
                request.rowLabel(),
                request.seatNumber())) {

            log.warn(
                    "Seat '{}{}' already exists in screen '{}' (screenId={})",
                    request.rowLabel(),
                    request.seatNumber(),
                    screen.getName(),
                    screenId
            );

            throw new ResourceAlreadyExistsException(
                    "Seat already exists in this screen"
            );
        }

        Seat seat = seatMapper.toEntity(request);

        seat.setScreen(screen);

        Seat savedSeat = seatRepository.save(seat);

        log.info(
                "Seat created successfully. seatId={}, seat='{}{}', screen='{}' (screenId={})",
                savedSeat.getId(),
                savedSeat.getRowLabel(),
                savedSeat.getSeatNumber(),
                screen.getName(),
                screenId
        );

        return seatMapper.toResponse(savedSeat);
    }

    @Override
    public List<SeatResponse> getSeatsByScreen(Long screenId) {

        log.info("Fetching active seats for screenId={}", screenId);

        Screen screen = screenRepository.findById(screenId)
                .orElseThrow(() -> {
                    log.warn(
                            "Cannot fetch seats. Screen not found. screenId={}",
                            screenId
                    );

                    return new ResourceNotFoundException(
                            "Screen not found with id: " + screenId
                    );
                });

        if (!Boolean.TRUE.equals(screen.getIsActive())) {

            log.warn(
                    "Cannot fetch seats. Screen is inactive. screenId={}",
                    screenId
            );

            throw new ResourceNotFoundException(
                    "Screen not found with id: " + screenId
            );
        }

        List<SeatResponse> responses =
                seatRepository.findAllByScreenIdAndIsActiveTrue(screenId)
                        .stream()
                        .map(seatMapper::toResponse)
                        .toList();

        log.info(
                "Found {} active seats for screenId={}",
                responses.size(),
                screenId
        );

        return responses;
    }

    @Override
    @Transactional
    public List<SeatResponse> createSeats(
            Long screenId,
            BulkSeatRequest request) {

        log.info(
                "Creating {} seats for screenId={}",
                request.seats().size(),
                screenId
        );

        Screen screen = screenRepository.findById(screenId)
                .orElseThrow(() -> {
                    log.warn(
                            "Cannot create seats. Screen not found. screenId={}",
                            screenId
                    );

                    return new ResourceNotFoundException(
                            "Screen not found with id: " + screenId
                    );
                });

        if (!Boolean.TRUE.equals(screen.getIsActive())) {

            log.warn(
                    "Cannot create seats. Screen is inactive. screenId={}",
                    screenId
            );

            throw new ResourceNotFoundException(
                    "Screen not found with id: " + screenId
            );
        }

        Set<String> requestedSeats = new HashSet<>();

        for (SeatRequest seatRequest : request.seats()) {

            String seatKey =
                    seatRequest.rowLabel().trim().toUpperCase()
                            + "-"
                            + seatRequest.seatNumber();

            if (!requestedSeats.add(seatKey)) {

                log.warn(
                        "Duplicate seat in bulk request. seat='{}{}', screenId={}",
                        seatRequest.rowLabel(),
                        seatRequest.seatNumber(),
                        screenId
                );

                throw new ResourceAlreadyExistsException(
                        "Duplicate seat in request: "
                                + seatRequest.rowLabel()
                                + seatRequest.seatNumber()
                );
            }

            if (seatRepository.existsByScreenIdAndRowLabelAndSeatNumber(
                    screenId,
                    seatRequest.rowLabel(),
                    seatRequest.seatNumber())) {

                log.warn(
                        "Seats '{}{}' already exists in screen '{}' (screenId={})",
                        seatRequest.rowLabel(),
                        seatRequest.seatNumber(),
                        screen.getName(),
                        screenId
                );

                throw new ResourceAlreadyExistsException(
                        "Seat already exists in this screen: "
                                + seatRequest.rowLabel()
                                + seatRequest.seatNumber()
                );
            }
        }

        List<Seat> seats = new ArrayList<>();

        for (SeatRequest seatRequest : request.seats()) {

            Seat seat = seatMapper.toEntity(seatRequest);

            seat.setScreen(screen);

            seats.add(seat);
        }

        List<Seat> savedSeats =
                seatRepository.saveAll(seats);

        log.info(
                "Successfully created {} seats for screen '{}' (screenId={})",
                savedSeats.size(),
                screen.getName(),
                screenId
        );

        return savedSeats.stream()
                .map(seatMapper::toResponse)
                .toList();
    }
}