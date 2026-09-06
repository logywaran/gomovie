package com.gomovie.show;

import com.gomovie.common.exception.InvalidStateException;
import com.gomovie.common.exception.ResourceAlreadyExistsException;
import com.gomovie.common.exception.ResourceNotFoundException;
import com.gomovie.movie.Movie;
import com.gomovie.movie.MovieRepository;
import com.gomovie.screen.Screen;
import com.gomovie.screen.ScreenRepository;
import com.gomovie.showseat.ShowSeatService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MovieShowServiceImpl implements MovieShowService {

    private final MovieShowRepository movieShowRepository;
    private final MovieRepository movieRepository;
    private final ScreenRepository screenRepository;
    private final MovieShowMapper movieShowMapper;
    private final ShowSeatService showSeatService;

    @Override
    @Transactional
    public MovieShowResponse createShow(
            MovieShowRequest request,
            Long managerId) {

        /*
         * A show can only be created for an existing active movie.
         * An inactive movie should not receive new show schedules.
         */
        Movie movie = movieRepository.findById(request.movieId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Movie not found with id: "
                                        + request.movieId()
                        )
                );

        if (!Boolean.TRUE.equals(movie.getIsActive())) {
            throw new ResourceNotFoundException(
                    "Movie not found with id: " + request.movieId()
            );
        }

        /*
         * The screen must exist because the show is physically
         * scheduled on a particular screen.
         */
        Screen screen = screenRepository.findById(request.screenId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Screen not found with id: "
                                        + request.screenId()
                        )
                );

        /*
         * SecurityConfig verifies that the user has the
         * THEATRE_MANAGER role.
         *
         * This service-level check verifies resource ownership:
         * the manager must own the theatre containing this screen.
         */
        if (!screen.getTheatre()
                .getManager()
                .getId()
                .equals(managerId)) {

            throw new AccessDeniedException(
                    "You do not have access to this screen"
            );
        }

        /*
         * An inactive screen cannot be used to create a new show.
         */
        if (!Boolean.TRUE.equals(screen.getIsActive())) {
            throw new ResourceNotFoundException(
                    "Screen not found with id: " + request.screenId()
            );
        }

        /*
         * A show must have a positive duration.
         *
         * Example:
         * 10:00 -> 13:00  valid
         * 13:00 -> 10:00  invalid
         * 10:00 -> 10:00  invalid
         */
        if (!request.startTime().isBefore(request.endTime())) {

            throw new IllegalArgumentException(
                    "Start time must be before end time"
            );
        }

        /*
         * A show scheduled for today cannot start in the past.
         *
         * Future dates are allowed with any valid future schedule.
         */
        if (request.showDate().isEqual(LocalDate.now())
                && request.startTime().isBefore(LocalTime.now())) {

            throw new IllegalArgumentException(
                    "Show start time cannot be in the past"
            );
        }

        /*
         * A mandatory 15-minute buffer is required between shows
         * on the same screen.
         *
         * Instead of checking only the actual show interval,
         * we expand the requested interval by 15 minutes on both sides.
         *
         * Example:
         *
         * Existing show: 10:00 - 13:00
         *
         * New show:      13:15 - 16:00  -> allowed
         * New show:      13:14 - 16:00  -> conflict
         */
        boolean conflict =
                movieShowRepository.existsConflictingShow(
                        request.screenId(),
                        request.showDate(),
                        request.startTime().minusMinutes(15),
                        request.endTime().plusMinutes(15)
                );

        if (conflict) {

            throw new ResourceAlreadyExistsException(
                    "Show conflicts with an existing show on this screen"
            );
        }

        /*
         * All validations have passed.
         * The mapper converts the request into a MovieShow entity
         * and attaches the existing Movie and Screen entities.
         */
        MovieShow movieShow =
                movieShowMapper.toEntity(
                        request,
                        movie,
                        screen
                );

        MovieShow savedMovieShow =
                movieShowRepository.save(movieShow);

        /*
         * Every physical seat on the screen needs a corresponding
         * ShowSeat record for this particular show.
         *
         * MovieShow = the scheduled show
         * ShowSeat  = seat inventory for that specific show
         */
        showSeatService.generateShowSeats(
                savedMovieShow.getId()
        );

        return movieShowMapper.toResponse(savedMovieShow);
    }

    @Override
    public MovieShowResponse getShowById(Long id) {

        MovieShow movieShow = movieShowRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Show not found with id: " + id
                        )
                );

        return movieShowMapper.toResponse(movieShow);
    }

    @Override
    public List<MovieShowResponse> getAllShows() {

        return movieShowRepository.findAll()
                .stream()
                .map(movieShowMapper::toResponse)
                .toList();
    }

    @Override
    public List<MovieShowResponse> getShowsForCustomer(
            Long movieId,
            Long theatreId) {

        /*
         * Customer browsing is based on:
         *
         * Movie + Theatre + Date
         *
         * The repository returns only active shows and
         * orders them by start time.
         */
        return movieShowRepository
                .findActiveShowsForCustomer(
                        movieId,
                        theatreId
                )
                .stream()
                .map(movieShowMapper::toResponse)
                .toList();
    }

    @Override
    public MovieShowResponse deactivateShow(
            Long id,
            Long managerId) {

        MovieShow movieShow =
                movieShowRepository.findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Show not found with id: " + id
                                )
                        );

        /*
         * SecurityConfig checks the manager's role.
         * Here we check whether this specific show belongs
         * to a theatre owned by the logged-in manager.
         */
        if (!movieShow.getScreen()
                .getTheatre()
                .getManager()
                .getId()
                .equals(managerId)) {

            throw new AccessDeniedException(
                    "You do not have access to this show"
            );
        }

        /*
         * Prevent an invalid state transition:
         * active -> inactive is valid,
         * inactive -> inactive is not.
         */
        if (!Boolean.TRUE.equals(movieShow.getIsActive())) {

            throw new InvalidStateException(
                    "Show is already inactive"
            );
        }

        /*
         * We deactivate instead of deleting the show.
         *
         * This preserves the show and its related ShowSeat/
         * booking history.
         */
        movieShow.setIsActive(false);

        MovieShow updatedMovieShow =
                movieShowRepository.save(movieShow);

        return movieShowMapper.toResponse(updatedMovieShow);
    }

    @Override
    public MovieShowResponse reactivateShow(
            Long id,
            Long managerId) {

        MovieShow movieShow =
                movieShowRepository.findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Show not found with id: " + id
                                )
                        );

        /*
         * The manager must own the theatre containing
         * the screen on which this show is scheduled.
         */
        if (!movieShow.getScreen()
                .getTheatre()
                .getManager()
                .getId()
                .equals(managerId)) {

            throw new AccessDeniedException(
                    "You do not have access to this show"
            );
        }

        /*
         * Prevent an invalid state transition:
         * inactive -> active is valid,
         * active -> active is not.
         */
        if (Boolean.TRUE.equals(movieShow.getIsActive())) {

            throw new InvalidStateException(
                    "Show is already active"
            );
        }

        /*
         * The screen may have been deactivated while
         * this show was inactive.
         *
         * An inactive screen cannot host an active show.
         */
        if (!Boolean.TRUE.equals(
                movieShow.getScreen().getIsActive())) {

            throw new InvalidStateException(
                    "Cannot reactivate show because its screen is inactive"
            );
        }

        /*
         * The schedule must be checked again during reactivation.
         *
         * Why?
         *
         * When this show was inactive, another show could have
         * been created in the same time slot.
         *
         * Therefore, reactivation must obey the same 15-minute
         * buffer rule as show creation.
         *
         * The current show is excluded from the conflict check.
         */
        boolean conflict =
                movieShowRepository
                        .existsConflictingShowExcludingId(
                                movieShow.getId(),
                                movieShow.getScreen().getId(),
                                movieShow.getShowDate(),
                                movieShow.getStartTime()
                                        .minusMinutes(15),
                                movieShow.getEndTime()
                                        .plusMinutes(15)
                        );

        if (conflict) {

            throw new ResourceAlreadyExistsException(
                    "Show conflicts with an existing show on this screen"
            );
        }

        movieShow.setIsActive(true);

        MovieShow updatedMovieShow =
                movieShowRepository.save(movieShow);

        return movieShowMapper.toResponse(updatedMovieShow);
    }
}