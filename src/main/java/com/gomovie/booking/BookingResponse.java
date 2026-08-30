package com.gomovie.booking;

import com.gomovie.bookingseat.BookingSeatResponse;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record BookingResponse(

        Long id,

        String bookingReference,

        Long userId,

        Long showId,

        BookingStatus status,

        BigDecimal totalAmount,

        LocalDateTime holdExpiresAt,

        List<BookingSeatResponse> seats

) {
}