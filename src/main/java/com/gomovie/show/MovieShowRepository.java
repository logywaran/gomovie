package com.gomovie.show;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;

public interface MovieShowRepository
        extends JpaRepository<MovieShow, Long> {

    @Query("""
            SELECT COUNT(ms) > 0
            FROM MovieShow ms
            WHERE ms.screen.id = :screenId
              AND ms.showDate = :showDate
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
            WHERE ms.screen.id = :screenId
              AND ms.showDate = :showDate
              AND ms.id <> :showId
              AND ms.startTime < :endTime
              AND ms.endTime > :startTime
            """)
    boolean existsConflictingShowExcludingId(
            @Param("screenId") Long screenId,
            @Param("showDate") LocalDate showDate,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime,
            @Param("showId") Long showId
    );
}

