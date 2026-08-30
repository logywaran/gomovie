package com.gomovie.booking;

import com.gomovie.bookingseat.BookingSeat;
import com.gomovie.bookingseat.BookingSeatRepository;
import com.gomovie.showseat.ShowSeat;
import com.gomovie.showseat.ShowSeatRepository;
import com.gomovie.showseat.ShowSeatStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class BookingExpiryScheduler {

    private final BookingRepository bookingRepository;
    private final BookingSeatRepository bookingSeatRepository;
    private final ShowSeatRepository showSeatRepository;

    @Scheduled(fixedRate = 30000)
    @Transactional
    public void expireBookings() {

        LocalDateTime now = LocalDateTime.now();

        List<Booking> expiredBookings =
                bookingRepository.findByStatusAndHoldExpiresAtBefore(
                        BookingStatus.PENDING,
                        now
                );

        for (Booking booking : expiredBookings) {

            List<BookingSeat> bookingSeats =
                    bookingSeatRepository.findByBookingId(
                            booking.getId()
                    );

            for (BookingSeat bookingSeat : bookingSeats) {

                ShowSeat showSeat =
                        showSeatRepository.findById(
                                bookingSeat.getShowSeatId()
                        ).orElse(null);

                if (showSeat != null
                        && showSeat.getStatus() == ShowSeatStatus.HELD) {

                    showSeat.setStatus(
                            ShowSeatStatus.AVAILABLE
                    );
                }
            }

            booking.setStatus(
                    BookingStatus.EXPIRED
            );

            log.info(
                    "Booking expired: {}",
                    booking.getBookingReference()
            );
        }
    }
}