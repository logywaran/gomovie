package com.gomovie.show;

import com.gomovie.movie.Movie;
import com.gomovie.theatre.Theatre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface MovieShowRepository
        extends JpaRepository<MovieShow, Long> {

    @Query("""
        SELECT COUNT(ms) > 0
        FROM MovieShow ms
        WHERE ms.screen.id = :screenId
          AND ms.showDate = :showDate
          AND ms.isActive = true
          AND ms.startTime < :endTime
          AND ms.endTime > :startTime
        """)
    boolean existsConflictingShow(
            @Param("screenId") Long screenId,
            @Param("showDate") LocalDate showDate,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime
    );

    @Query("""
        SELECT COUNT(ms) > 0
        FROM MovieShow ms
        WHERE ms.id <> :showId
          AND ms.screen.id = :screenId
          AND ms.showDate = :showDate
          AND ms.isActive = true
          AND ms.startTime < :endTime
          AND ms.endTime > :startTime
        """)
    boolean existsConflictingShowExcludingId(
            @Param("showId") Long showId,
            @Param("screenId") Long screenId,
            @Param("showDate") LocalDate showDate,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime
    );

    @Query("""
    SELECT ms
    FROM MovieShow ms
    WHERE ms.movie.id = :movieId
      AND ms.screen.theatre.id = :theatreId
      AND ms.showDate = :showDate
      AND ms.isActive = true
    ORDER BY ms.startTime
    """)
    List<MovieShow> findActiveShowsForCustomer(
            @Param("movieId") Long movieId,
            @Param("theatreId") Long theatreId,
            @Param("showDate") LocalDate showDate
    );

    @Query("""
        SELECT DISTINCT ms.movie
        FROM MovieShow ms
        WHERE ms.screen.theatre.city.id = :cityId
          AND ms.isActive = true
          AND ms.movie.isActive = true
        """)
    List<Movie> findActiveMoviesByCity(
            @Param("cityId") Long cityId
    );

    @Query("""
    SELECT DISTINCT ms.screen.theatre
    FROM MovieShow ms
    WHERE ms.movie.id = :movieId
      AND ms.screen.theatre.city.id = :cityId
      AND ms.isActive = true
      AND ms.movie.isActive = true
      AND ms.screen.theatre.isActive = true
    """)
    List<Theatre> findActiveTheatresByMovieAndCity(
            @Param("movieId") Long movieId,
            @Param("cityId") Long cityId
    );
}
