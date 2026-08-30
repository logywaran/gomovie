package com.gomovie.booking;

import com.gomovie.bookingseat.BookingSeat;
import com.gomovie.bookingseat.BookingSeatResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BookingMapper {

    public BookingResponse toResponse(
            Booking booking,
            List<BookingSeat> bookingSeats) {

        List<BookingSeatResponse> seats =
                bookingSeats.stream()
                        .map(bookingSeat ->
                                new BookingSeatResponse(
                                        bookingSeat.getShowSeatId(),
                                        bookingSeat.getShowSeat()
                                                .getSeat()
                                                .getRowLabel(),
                                        bookingSeat.getShowSeat()
                                                .getSeat()
                                                .getSeatNumber(),
                                        bookingSeat.getShowSeat()
                                                .getSeat()
                                                .getSeatType(),
                                        bookingSeat.getPrice()
                                )
                        )
                        .toList();

        return new BookingResponse(
                booking.getId(),
                booking.getBookingReference(),
                booking.getUser().getId(),
                booking.getShow().getId(),
                booking.getStatus(),
                booking.getTotalAmount(),
                booking.getHoldExpiresAt(),
                seats
        );
    }
}