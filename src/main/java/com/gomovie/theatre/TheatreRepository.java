package com.gomovie.theatre;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TheatreRepository extends JpaRepository<Theatre, Long> {

    boolean existsByCityIdAndNameIgnoreCase(
            Long cityId,
            String name
    );

    List<Theatre> findByCityIdAndIsActiveTrue(
            Long cityId
    );

    boolean existsByCityIdAndNameIgnoreCaseAndIdNot(
            Long cityId,
            String name,
            Long theatreId
    );

    List<Theatre> findByManagerId(Long managerId);
}