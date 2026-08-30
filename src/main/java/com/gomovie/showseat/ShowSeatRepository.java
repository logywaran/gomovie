package com.gomovie.showseat;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ShowSeatRepository
        extends JpaRepository<ShowSeat, Long> {

    List<ShowSeat> findByShowId(Long showId);

    boolean existsByShowId(Long showId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT ss
            FROM ShowSeat ss
            WHERE ss.id IN :showSeatIds
            ORDER BY ss.id
            """)
    List<ShowSeat> findAllByIdForUpdate(
            @Param("showSeatIds") List<Long> showSeatIds
    );
}