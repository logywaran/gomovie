package com.gomovie.booking;

import java.util.List;

public interface BookingService {

    BookingResponse createBooking(
            BookingRequest request
    );

    BookingResponse getBookingById(
            Long id
    );

    List<BookingResponse> getMyBookings();
}