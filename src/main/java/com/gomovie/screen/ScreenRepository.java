package com.gomovie.screen;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScreenRepository extends JpaRepository<Screen, Long> {

    boolean existsByTheatreIdAndNameIgnoreCase(
            Long theatreId,
            String name
    );

    boolean existsByTheatreIdAndNameIgnoreCaseAndIdNot(
            Long theatreId,
            String name,
            Long screenId
    );

    List<Screen> findAllByTheatreId(Long theatreId);
}