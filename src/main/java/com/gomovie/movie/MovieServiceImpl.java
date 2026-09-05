package com.gomovie.movie;

import com.gomovie.common.exception.InvalidStateException;
import com.gomovie.common.exception.ResourceAlreadyExistsException;
import com.gomovie.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MovieServiceImpl implements MovieService {

    private final MovieRepository movieRepository;
    private final MovieMapper movieMapper;

    // Create a new movie.
    @Override
    public MovieResponse createMovie(MovieRequest request) {

        String movieTitle = request.getTitle().trim();

        log.info(
                "Creating movie: title={}",
                movieTitle
        );

        // Check whether a movie with the same title already exists.
        if (movieRepository.existsByTitleIgnoreCase(movieTitle)) {

            log.warn(
                    "Movie creation rejected: title={} already exists",
                    movieTitle
            );

            throw new ResourceAlreadyExistsException(
                    "Movie already exists with title: " + movieTitle
            );
        }

        // Convert the request DTO into a Movie entity.
        Movie movie = movieMapper.toEntity(request);

        // Use the normalized title after trimming whitespace.
        movie.setTitle(movieTitle);

        // A newly created movie starts as active.
        // Deactivation is handled separately by the movie lifecycle operations.
        movie.setIsActive(true);

        // Save the movie to the database.
        Movie savedMovie = movieRepository.save(movie);

        log.info(
                "Movie created successfully: id={}, title={}",
                savedMovie.getId(),
                savedMovie.getTitle()
        );

        // Convert the saved entity into the response DTO.
        return movieMapper.toResponse(savedMovie);
    }

    // Return an active movie for customer-facing APIs.
    @Override
    @Transactional(readOnly = true)
    public MovieResponse getMovieById(Long id) {

        log.debug("Fetching active movie: id={}", id);

        // Find the movie only if it is currently active.
        // Inactive movies should not be exposed through the public movie API.
        Movie movie = movieRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> {

                    log.warn(
                            "Movie fetch failed: id={} not found or inactive",
                            id
                    );

                    return new ResourceNotFoundException(
                            "Movie not found with id: " + id
                    );
                });

        // Convert the entity into the response DTO.
        return movieMapper.toResponse(movie);
    }

    // Update the details of an existing movie.
    @Override
    public MovieResponse updateMovie(Long id, MovieUpdateRequest request) {

        log.info("Updating movie: id={}", id);

        // Find the movie before updating it.
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> {

                    log.warn(
                            "Movie update failed: id={} not found",
                            id
                    );

                    return new ResourceNotFoundException(
                            "Movie not found with id: " + id
                    );
                });

        String movieTitle = request.getTitle().trim();

        // Check whether another movie already uses the same title.
        // The current movie is excluded from the check using its id.
        if (movieRepository.existsByTitleIgnoreCaseAndIdNot(
                movieTitle,
                id
        )) {

            log.warn(
                    "Movie update rejected: id={}, title={} already exists",
                    id,
                    movieTitle
            );

            throw new ResourceAlreadyExistsException(
                    "Movie already exists with title: " + movieTitle
            );
        }

        // Update only the fields that are allowed to change.
        movie.setTitle(movieTitle);
        movie.setDescription(request.getDescription());
        movie.setDurationMinutes(request.getDurationMinutes());
        movie.setReleaseDate(request.getReleaseDate());
        movie.setCertificate(request.getCertificate());
        movie.setPosterUrl(request.getPosterUrl());
        movie.setTrailerUrl(request.getTrailerUrl());

        // isActive and deletedAt are intentionally not changed here.
        // Movie lifecycle changes are handled by the dedicated
        // deactivateMovie() and reactivateMovie() operations.

        log.info(
                "Movie updated successfully: id={}, title={}",
                movie.getId(),
                movie.getTitle()
        );

        // No save() is required here.
        // The movie is a managed JPA entity inside the current transaction.
        // JPA dirty checking detects the changes and updates the database
        // when the transaction is committed.

        return movieMapper.toResponse(movie);
    }

    // Soft-delete a movie by marking it inactive.
    @Override
    public MovieResponse deactivateMovie(Long id) {

        log.info("Deactivating movie: id={}", id);

        // Find the movie before changing its state.
        // We use findById() here because an inactive movie must also be
        // found so that we can detect an invalid repeated deactivation.
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> {

                    log.warn(
                            "Movie deactivation failed: id={} not found",
                            id
                    );

                    return new ResourceNotFoundException(
                            "Movie not found with id: " + id
                    );
                });

        // Prevent deactivating an already inactive movie.
        if (!Boolean.TRUE.equals(movie.getIsActive())) {

            log.warn(
                    "Movie deactivation rejected: id={} is already inactive",
                    id
            );

            throw new InvalidStateException(
                    "Movie is already inactive"
            );
        }

        // Mark the movie as inactive.
        movie.setIsActive(false);

        // Record when the movie was deactivated.
        movie.setDeletedAt(LocalDateTime.now());

        // No save() is required here.
        // The movie is a managed JPA entity inside the current transaction.
        // JPA dirty checking detects these changes and updates the database
        // when the transaction is committed.

        log.info(
                "Movie deactivated successfully: id={}, title={}",
                movie.getId(),
                movie.getTitle()
        );

        // Convert the updated entity into the response DTO.
        return movieMapper.toResponse(movie);
    }

    // Reactivate a previously deactivated movie.
    @Override
    public MovieResponse reactivateMovie(Long id) {

        log.info("Reactivating movie: id={}", id);

        // Find the movie before changing its state.
        // findById() is required because the movie is currently inactive.
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> {

                    log.warn(
                            "Movie reactivation failed: id={} not found",
                            id
                    );

                    return new ResourceNotFoundException(
                            "Movie not found with id: " + id
                    );
                });

        // Prevent reactivating an already active movie.
        if (Boolean.TRUE.equals(movie.getIsActive())) {

            log.warn(
                    "Movie reactivation rejected: id={} is already active",
                    id
            );

            throw new InvalidStateException(
                    "Movie is already active"
            );
        }

        // Mark the movie as active again.
        movie.setIsActive(true);

        // Clear the previous deactivation timestamp.
        movie.setDeletedAt(null);

        // No save() is required here.
        // JPA dirty checking detects the changes because the entity
        // is managed inside the current transaction.

        log.info(
                "Movie reactivated successfully: id={}, title={}",
                movie.getId(),
                movie.getTitle()
        );

        // Convert the updated entity into the response DTO.
        return movieMapper.toResponse(movie);
    }

    // Return active movies for customer-facing APIs.
    // Supports optional certificate filtering and controlled sorting.
    @Override
    @Transactional(readOnly = true)
    public List<MovieResponse> getMovies(
            String certificate,
            String sort) {

        log.debug(
                "Fetching movies: certificate={}, sort={}",
                certificate,
                sort
        );

        // Convert the API sort option into a controlled Sort object.
        // Only the sorting options supported by the API are allowed.
        Sort movieSort = switch (sort == null ? "title" : sort) {

            case "title" ->
                    Sort.by("title").ascending();

            case "releaseDate" ->
                    Sort.by("releaseDate").descending();

            default -> {
                log.warn("Invalid movie sort option: {}", sort);

                throw new IllegalArgumentException(
                        "Invalid sort option: " + sort
                );
            }
        };

        List<Movie> movies;

        // If no certificate filter is provided,
        // fetch all active movies using the requested sort order.
        if (certificate == null || certificate.isBlank()) {

            movies = movieRepository.findByIsActiveTrue(movieSort);

        } else {

            // If a certificate filter is provided,
            // fetch only active movies matching that certificate.
            movies = movieRepository
                    .findByIsActiveTrueAndCertificateIgnoreCase(
                            certificate.trim(),
                            movieSort
                    );
        }

        // Convert Movie entities into response DTOs.
        return movies.stream()
                .map(movieMapper::toResponse)
                .toList();
    }

    // Return all movies for administrative movie management.
    // Unlike the public API, inactive movies are also included.
    @Override
    @Transactional(readOnly = true)
    public List<MovieResponse> getAllMoviesForAdmin() {

        log.debug("Fetching all movies for admin");

        // Admins need to see both active and inactive movies
        // so they can manage the complete movie catalogue.
        return movieRepository.findAll()
                .stream()
                .map(movieMapper::toResponse)
                .toList();
    }
}