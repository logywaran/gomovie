package com.gomovie.show;

import com.gomovie.common.exception.ResourceAlreadyExistsException;
import com.gomovie.common.exception.ResourceNotFoundException;
import com.gomovie.movie.Movie;
import com.gomovie.movie.MovieRepository;
import com.gomovie.screen.Screen;
import com.gomovie.screen.ScreenRepository;
import com.gomovie.showseat.ShowSeatService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
            MovieShowRequest request) {

        Movie movie = movieRepository.findById(request.movieId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Movie not found with id: "
                                        + request.movieId()
                        )
                );

        Screen screen = screenRepository.findById(request.screenId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Screen not found with id: "
                                        + request.screenId()
                        )
                );

        // A show must have a positive duration.
        if (!request.startTime().isBefore(request.endTime())) {

            throw new IllegalArgumentException(
                    "Start time must be before end time"
            );
        }

        /*
         * A 15-minute buffer is required between shows on the same screen.
         * The buffer is applied in both directions to prevent overlapping
         * or back-to-back shows without the required gap.
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

        MovieShow movieShow =
                movieShowMapper.toEntity(
                        request,
                        movie,
                        screen
                );

        MovieShow savedMovieShow =
                movieShowRepository.save(movieShow);

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
    public MovieShowResponse updateShow(
            Long id,
            MovieShowRequest request) {

        MovieShow movieShow =
                movieShowRepository.findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Show not found with id: " + id
                                )
                        );

        Movie movie =
                movieRepository.findById(request.movieId())
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Movie not found with id: "
                                                + request.movieId()
                                )
                        );

        Screen screen =
                screenRepository.findById(request.screenId())
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Screen not found with id: "
                                                + request.screenId()
                                )
                        );

        // A show must have a positive duration.
        if (!request.startTime().isBefore(request.endTime())) {

            throw new IllegalArgumentException(
                    "Start time must be before end time"
            );
        }

        /*
         * Check for conflicts while excluding the show being updated.
         * The 15-minute buffer is applied in both directions.
         */
        boolean conflict =
                movieShowRepository.existsConflictingShowExcludingId(
                        request.screenId(),
                        request.showDate(),
                        request.startTime().minusMinutes(15),
                        request.endTime().plusMinutes(15),
                        id
                );

        if (conflict) {

            throw new ResourceAlreadyExistsException(
                    "Show conflicts with an existing show on this screen"
            );
        }

        movieShowMapper.updateEntity(
                movieShow,
                request,
                movie,
                screen
        );

        MovieShow updatedMovieShow =
                movieShowRepository.save(movieShow);

        return movieShowMapper.toResponse(updatedMovieShow);
    }

    @Override
    public MovieShowResponse deactivateShow(Long id) {

        MovieShow movieShow =
                movieShowRepository.findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Show not found with id: " + id
                                )
                        );

        // A deactivated show should not be deactivated again.
        if (!movieShow.getIsActive()) {

            throw new ResourceAlreadyExistsException(
                    "Show is already inactive"
            );
        }

        movieShow.setIsActive(false);

        MovieShow updatedMovieShow =
                movieShowRepository.save(movieShow);

        return movieShowMapper.toResponse(updatedMovieShow);
    }
}