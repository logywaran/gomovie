package com.gomovie.movie;

import com.gomovie.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class MovieServiceImpl implements MovieService {

    private final MovieRepository movieRepository;
    private final MovieMapper movieMapper;

    @Override
    public MovieResponse createMovie(MovieRequest request) {

        log.info("Creating movie with title: {}", request.getTitle());

        Movie movie = movieMapper.toEntity(request);

        // A newly created movie starts as active.
        // Deactivation is handled separately by the movie lifecycle operation.
        movie.setIsActive(true);

        Movie savedMovie = movieRepository.save(movie);

        log.info("Movie created successfully with id: {}", savedMovie.getId());

        return movieMapper.toResponse(savedMovie);
    }

    @Override
    public MovieResponse getMovieById(Long id) {

        log.debug("Fetching movie with id: {}", id);

        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found with id: "+id));

        return movieMapper.toResponse(movie);
    }
}