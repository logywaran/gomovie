package com.gomovie.seat;

import com.gomovie.common.exception.InvalidStateException;
import com.gomovie.common.exception.ResourceAlreadyExistsException;
import com.gomovie.common.exception.ResourceNotFoundException;
import com.gomovie.screen.Screen;
import com.gomovie.screen.ScreenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class SeatServiceImpl implements SeatService {

    private final SeatRepository seatRepository;
    private final ScreenRepository screenRepository;
    private final SeatMapper seatMapper;

    // Create a single seat for a screen.
    @Override
    public SeatResponse createSeat(
            Long screenId,
            SeatRequest request,
            Long managerId) {

        log.info(
                "Creating seat '{}{}' of type '{}' for screenId={}",
                request.rowLabel(),
                request.seatNumber(),
                request.seatType(),
                screenId
        );

        // Find the screen to which the new seat will belong.
        Screen screen = screenRepository.findById(screenId)
                .orElseThrow(() -> {

                    log.warn(
                            "Seat creation failed: screenId={} not found",
                            screenId
                    );

                    return new ResourceNotFoundException(
                            "Screen not found with id: " + screenId
                    );
                });

        // Verify that the authenticated theatre manager owns
        // the theatre to which this screen belongs.
        if (!screen.getTheatre().getManager().getId().equals(managerId)) {

            log.warn(
                    "Seat creation rejected: managerId={} does not own screenId={}",
                    managerId,
                    screenId
            );

            throw new AccessDeniedException(
                    "You do not have access to this screen"
            );
        }

        // Seats cannot be added to an inactive screen.
        if (!Boolean.TRUE.equals(screen.getIsActive())) {

            log.warn(
                    "Seat creation rejected: screenId={} is inactive",
                    screenId
            );

            throw new ResourceNotFoundException(
                    "Screen not found with id: " + screenId
            );
        }

        // Normalize the row label before checking uniqueness.
        // For example: "  a  " becomes "A".
        String normalizedRowLabel =
                request.rowLabel().trim().toUpperCase();

        // Check whether the same physical seat already exists
        // in this screen.
        if (seatRepository.existsByScreenIdAndRowLabelAndSeatNumber(
                screenId,
                normalizedRowLabel,
                request.seatNumber())) {

            log.warn(
                    "Seat creation rejected: seat='{}{}' already exists in screenId={}",
                    normalizedRowLabel,
                    request.seatNumber(),
                    screenId
            );

            throw new ResourceAlreadyExistsException(
                    "Seat already exists in this screen"
            );
        }

        // Convert the request DTO into a Seat entity.
        Seat seat = seatMapper.toEntity(request);

        // Store the normalized row label.
        seat.setRowLabel(normalizedRowLabel);

        // Associate the seat with its screen.
        seat.setScreen(screen);

        // Save the new seat.
        Seat savedSeat = seatRepository.save(seat);

        log.info(
                "Seat created successfully: id={}, seat='{}{}', screenId={}",
                savedSeat.getId(),
                savedSeat.getRowLabel(),
                savedSeat.getSeatNumber(),
                screenId
        );

        // Convert the saved entity into a response DTO.
        return seatMapper.toResponse(savedSeat);
    }

    // Return all seats belonging to a screen for the theatre manager.
    // Both active and inactive seats are returned because the manager
    // needs to see and manage the complete physical seat layout.
    @Override
    public List<SeatResponse> getSeatsByScreen(
            Long screenId,
            Long managerId) {

        log.info(
                "Fetching all seats for screenId={}",
                screenId
        );

        // Find the screen before accessing its seats.
        Screen screen = screenRepository.findById(screenId)
                .orElseThrow(() -> {

                    log.warn(
                            "Seat fetch failed: screenId={} not found",
                            screenId
                    );

                    return new ResourceNotFoundException(
                            "Screen not found with id: " + screenId
                    );
                });

        // Verify that the manager owns the screen.
        if (!screen.getTheatre().getManager().getId().equals(managerId)) {

            log.warn(
                    "Seat fetch rejected: managerId={} does not own screenId={}",
                    managerId,
                    screenId
            );

            throw new AccessDeniedException(
                    "You do not have access to this screen"
            );
        }

        // Seats cannot be managed through an inactive screen.
        if (!Boolean.TRUE.equals(screen.getIsActive())) {

            log.warn(
                    "Seat fetch rejected: screenId={} is inactive",
                    screenId
            );

            throw new ResourceNotFoundException(
                    "Screen not found with id: " + screenId
            );
        }

        // Fetch all seats, including inactive seats.
        List<SeatResponse> responses =
                seatRepository.findByScreenId(screenId)
                        .stream()
                        .map(seatMapper::toResponse)
                        .toList();

        log.info(
                "Found {} seats for screenId={}",
                responses.size(),
                screenId
        );

        return responses;
    }

    // Create multiple seats in a single request.
    // The operation is transactional so that the complete bulk operation
    // succeeds or fails as one unit.
    @Override
    @Transactional
    public List<SeatResponse> createSeats(
            Long screenId,
            BulkSeatRequest request,
            Long managerId) {

        log.info(
                "Creating {} seats for screenId={}",
                request.seats().size(),
                screenId
        );

        // Find the screen to which all requested seats will belong.
        Screen screen = screenRepository.findById(screenId)
                .orElseThrow(() -> {

                    log.warn(
                            "Bulk seat creation failed: screenId={} not found",
                            screenId
                    );

                    return new ResourceNotFoundException(
                            "Screen not found with id: " + screenId
                    );
                });

        // Verify that the authenticated manager owns the screen.
        if (!screen.getTheatre().getManager().getId().equals(managerId)) {

            log.warn(
                    "Bulk seat creation rejected: managerId={} does not own screenId={}",
                    managerId,
                    screenId
            );

            throw new AccessDeniedException(
                    "You do not have access to this screen"
            );
        }

        // Seats cannot be added to an inactive screen.
        if (!Boolean.TRUE.equals(screen.getIsActive())) {

            log.warn(
                    "Bulk seat creation rejected: screenId={} is inactive",
                    screenId
            );

            throw new ResourceNotFoundException(
                    "Screen not found with id: " + screenId
            );
        }

        // Keep track of seats already present in the current request.
        // This catches duplicates before anything is saved.
        Set<String> requestedSeats = new HashSet<>();

        for (SeatRequest seatRequest : request.seats()) {

            // Normalize the row label so that values such as
            // "A", "a", and "  A  " are treated as the same row.
            String normalizedRowLabel =
                    seatRequest.rowLabel().trim().toUpperCase();

            // Create a unique key for the requested seat.
            String seatKey =
                    normalizedRowLabel
                            + "-"
                            + seatRequest.seatNumber();

            // Detect duplicate seats inside the same bulk request.
            if (!requestedSeats.add(seatKey)) {

                log.warn(
                        "Bulk seat creation rejected: duplicate seat='{}{}' in request, screenId={}",
                        normalizedRowLabel,
                        seatRequest.seatNumber(),
                        screenId
                );

                throw new ResourceAlreadyExistsException(
                        "Duplicate seat in request: "
                                + normalizedRowLabel
                                + seatRequest.seatNumber()
                );
            }

            // Check whether the requested seat already exists
            // in the database for this screen.
            if (seatRepository.existsByScreenIdAndRowLabelAndSeatNumber(
                    screenId,
                    normalizedRowLabel,
                    seatRequest.seatNumber())) {

                log.warn(
                        "Bulk seat creation rejected: seat='{}{}' already exists in screenId={}",
                        normalizedRowLabel,
                        seatRequest.seatNumber(),
                        screenId
                );

                throw new ResourceAlreadyExistsException(
                        "Seat already exists in this screen: "
                                + normalizedRowLabel
                                + seatRequest.seatNumber()
                );
            }
        }

        // Convert all validated requests into Seat entities.
        List<Seat> seats = new ArrayList<>();

        for (SeatRequest seatRequest : request.seats()) {

            // Apply the same normalization used during validation.
            String normalizedRowLabel =
                    seatRequest.rowLabel().trim().toUpperCase();

            Seat seat = seatMapper.toEntity(seatRequest);

            // Store the normalized row label.
            seat.setRowLabel(normalizedRowLabel);

            // Associate the seat with the screen.
            seat.setScreen(screen);

            seats.add(seat);
        }

        // Save all seats together.
        List<Seat> savedSeats =
                seatRepository.saveAll(seats);

        log.info(
                "Bulk seat creation successful: {} seats created for screenId={}",
                savedSeats.size(),
                screenId
        );

        // Convert the saved entities into response DTOs.
        return savedSeats.stream()
                .map(seatMapper::toResponse)
                .toList();
    }

    // Soft-delete a seat by marking it inactive.
    // The seat record is retained so that historical references remain valid.
    @Override
    public void deactivateSeat(
            Long seatId,
            Long managerId) {

        log.info(
                "Deactivating seat: seatId={}, managerId={}",
                seatId,
                managerId
        );

        // Find the seat before changing its lifecycle state.
        Seat seat = seatRepository.findById(seatId)
                .orElseThrow(() -> {

                    log.warn(
                            "Seat deactivation failed: seatId={} not found",
                            seatId
                    );

                    return new ResourceNotFoundException(
                            "Seat not found with id: " + seatId
                    );
                });

        // Verify ownership through the relationship:
        // Seat → Screen → Theatre → Manager.
        if (!seat.getScreen()
                .getTheatre()
                .getManager()
                .getId()
                .equals(managerId)) {

            log.warn(
                    "Seat deactivation rejected: managerId={} does not own seatId={}",
                    managerId,
                    seatId
            );

            throw new AccessDeniedException(
                    "You do not have access to this seat"
            );
        }

        // Prevent an already inactive seat from being deactivated again.
        if (!Boolean.TRUE.equals(seat.getIsActive())) {

            log.warn(
                    "Seat deactivation rejected: seatId={} is already inactive",
                    seatId
            );

            throw new InvalidStateException(
                    "Seat is already inactive"
            );
        }

        // Mark the seat as inactive.
        seat.setIsActive(false);

        // Record when the seat was deactivated.
        seat.setDeletedAt(LocalDateTime.now());

        // Save the lifecycle change.
        seatRepository.save(seat);

        log.info(
                "Seat deactivated successfully: seatId={}",
                seatId
        );
    }

    // Reactivate a previously deactivated seat.
    @Override
    public void reactivateSeat(
            Long seatId,
            Long managerId) {

        log.info(
                "Reactivating seat: seatId={}, managerId={}",
                seatId,
                managerId
        );

        // Find the seat before changing its lifecycle state.
        // findById() is required because the seat may currently be inactive.
        Seat seat = seatRepository.findById(seatId)
                .orElseThrow(() -> {

                    log.warn(
                            "Seat reactivation failed: seatId={} not found",
                            seatId
                    );

                    return new ResourceNotFoundException(
                            "Seat not found with id: " + seatId
                    );
                });

        // Verify ownership through:
        // Seat → Screen → Theatre → Manager.
        if (!seat.getScreen()
                .getTheatre()
                .getManager()
                .getId()
                .equals(managerId)) {

            log.warn(
                    "Seat reactivation rejected: managerId={} does not own seatId={}",
                    managerId,
                    seatId
            );

            throw new AccessDeniedException(
                    "You do not have access to this seat"
            );
        }

        // Prevent an already active seat from being reactivated.
        if (Boolean.TRUE.equals(seat.getIsActive())) {

            log.warn(
                    "Seat reactivation rejected: seatId={} is already active",
                    seatId
            );

            throw new InvalidStateException(
                    "Seat is already active"
            );
        }

        // Mark the seat as active again.
        seat.setIsActive(true);

        // Clear the previous deactivation timestamp.
        seat.setDeletedAt(null);

        // Save the lifecycle change.
        seatRepository.save(seat);

        log.info(
                "Seat reactivated successfully: seatId={}",
                seatId
        );
    }
}