package com.gomovie.city;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CityRepository extends JpaRepository<City, Long> {

    boolean existsByName(String name);

    List<City> findByIsActiveTrue();
}