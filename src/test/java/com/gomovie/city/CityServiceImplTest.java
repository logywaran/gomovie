package com.gomovie.city;

import com.gomovie.common.exception.ResourceAlreadyExistsException;
import com.gomovie.common.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CityServiceImplTest {

    @Mock
    private CityRepository cityRepository;

    @Mock
    private CityMapper cityMapper;

    private CityServiceImpl cityService;

    @BeforeEach
    void setUp() {

        cityService = new CityServiceImpl(
                cityRepository,
                cityMapper
        );
    }

    @Test
    void create_shouldCreateCitySuccessfully() {

        // Arrange
        CityRequest request = new CityRequest("Chennai");

        when(cityRepository.existsByNameIgnoreCase("Chennai"))
                .thenReturn(false);

        CityResponse expectedResponse =
                new CityResponse(1L, "Chennai");

        when(cityMapper.toResponse(any(City.class)))
                .thenReturn(expectedResponse);

        // Act
        CityResponse actualResponse =
                cityService.create(request);

        // Assert
        assertEquals(1L, actualResponse.id());
        assertEquals("Chennai", actualResponse.name());

        verify(cityRepository)
                .existsByNameIgnoreCase("Chennai");

        verify(cityRepository)
                .save(any(City.class));

        verify(cityMapper)
                .toResponse(any(City.class));
    }
}