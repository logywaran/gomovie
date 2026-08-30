package com.gomovie.seat;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SeatRepository extends JpaRepository<Seat, Long> {

    boolean existsByScreenIdAndRowLabelAndSeatNumber(
            Long screenId,
            String rowLabel,
            Integer seatNumber
    );


    List<Seat> findAllByScreenIdAndIsActiveTrue(Long screenId);

    List<Seat> findByScreenId(Long screenId);
}