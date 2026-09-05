package com.gomovie.movie;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MovieRepository extends JpaRepository<Movie, Long> {

    Optional<Movie> findByIdAndIsActiveTrue(Long id);

    List<Movie> findByIsActiveTrue(Sort sort);

    List<Movie> findByIsActiveTrueAndCertificateIgnoreCase(
            String certificate,
            Sort sort
    );

    boolean existsByTitleIgnoreCase(String title);

    boolean existsByTitleIgnoreCaseAndIdNot(
            String title,
            Long id
    );
}