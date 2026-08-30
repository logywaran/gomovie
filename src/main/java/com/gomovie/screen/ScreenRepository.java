package com.gomovie.screen;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScreenRepository extends JpaRepository<Screen, Long> {

    boolean existsByTheatreIdAndName(Long theatreId, String name);

    List<Screen> findAllByTheatreIdAndIsActiveTrue(Long theatreId);
}