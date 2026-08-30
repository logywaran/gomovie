package com.gomovie.booking;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository
        extends JpaRepository<Booking, Long> {

    Optional<Booking> findByBookingReference(String bookingReference);

    boolean existsByBookingReference(String bookingReference);

    List<Booking> findByStatusAndHoldExpiresAtBefore(
            BookingStatus status,
            LocalDateTime time
    );
}