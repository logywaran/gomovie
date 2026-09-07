package com.gomovie.show;

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
      AND ms.isActive = true
      AND ms.showDate >= :today
    ORDER BY ms.showDate, ms.startTime
    """)
    List<MovieShow> findActiveShowsForCustomer(
            @Param("movieId") Long movieId,
            @Param("theatreId") Long theatreId,
            @Param("today") LocalDate today
    );
}

