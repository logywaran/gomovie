package com.gomovie.booking;

import com.gomovie.bookingseat.BookingSeat;
import com.gomovie.bookingseat.BookingSeatRepository;
import com.gomovie.common.exception.ResourceAlreadyExistsException;
import com.gomovie.common.exception.ResourceNotFoundException;
import com.gomovie.show.MovieShow;
import com.gomovie.show.MovieShowRepository;
import com.gomovie.showseat.ShowSeat;
import com.gomovie.showseat.ShowSeatRepository;
import com.gomovie.showseat.ShowSeatStatus;
import com.gomovie.user.User;
import com.gomovie.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private static final int HOLD_DURATION_MINUTES = 5;

    private final BookingRepository bookingRepository;
    private final BookingSeatRepository bookingSeatRepository;
    private final MovieShowRepository movieShowRepository;
    private final ShowSeatRepository showSeatRepository;
    private final UserRepository userRepository;
    private final BookingMapper bookingMapper;

    @Override
    @Transactional
    public BookingResponse createBooking(
            BookingRequest request) {

        MovieShow show =
                movieShowRepository.findById(request.showId())
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Show not found with id: "
                                                + request.showId()
                                )
                        );

        if (!show.getIsActive()) {

            throw new ResourceAlreadyExistsException(
                    "Show is not active"
            );
        }

        /*
         * The authenticated user will be obtained from
         * Spring Security once authentication is connected.
         *
         * For now, this part will be completed when the
         * authentication flow is integrated.
         */
        User user =
                userRepository.findById(1L)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "User not found"
                                )
                        );

        List<ShowSeat> showSeats =
                showSeatRepository.findAllByIdForUpdate(
                        request.showSeatIds()
                );

        if (showSeats.size() != request.showSeatIds().size()) {

            throw new ResourceNotFoundException(
                    "One or more selected seats were not found"
            );
        }

        for (ShowSeat showSeat : showSeats) {

            if (!showSeat.getShowId().equals(show.getId())) {

                throw new ResourceAlreadyExistsException(
                        "Selected seat does not belong to the requested show"
                );
            }

            if (showSeat.getStatus() != ShowSeatStatus.AVAILABLE) {

                throw new ResourceAlreadyExistsException(
                        "One or more selected seats are not available"
                );
            }
        }

        LocalDateTime holdExpiresAt =
                LocalDateTime.now()
                        .plusMinutes(HOLD_DURATION_MINUTES);

        BigDecimal totalAmount = BigDecimal.ZERO;

        for (ShowSeat showSeat : showSeats) {

            showSeat.setStatus(ShowSeatStatus.HELD);

            totalAmount =
                    totalAmount.add(showSeat.getPrice());
        }

        showSeatRepository.saveAll(showSeats);

        Booking booking = new Booking();

        booking.setBookingReference(
                "GM-" + UUID.randomUUID()
                        .toString()
                        .replace("-", "")
                        .substring(0, 12)
                        .toUpperCase()
        );

        booking.setUser(user);
        booking.setShowId(show.getId());
        booking.setShow(show);
        booking.setStatus(BookingStatus.PENDING);
        booking.setTotalAmount(totalAmount);
        booking.setHoldExpiresAt(holdExpiresAt);

        Booking savedBooking =
                bookingRepository.save(booking);

        List<BookingSeat> bookingSeats =
                showSeats.stream()
                        .map(showSeat -> {

                            BookingSeat bookingSeat =
                                    new BookingSeat();

                            bookingSeat.setBookingId(
                                    savedBooking.getId()
                            );

                            bookingSeat.setShowId(
                                    show.getId()
                            );

                            bookingSeat.setShowSeatId(
                                    showSeat.getId()
                            );

                            bookingSeat.setBooking(
                                    savedBooking
                            );

                            bookingSeat.setShowSeat(
                                    showSeat
                            );

                            bookingSeat.setPrice(
                                    showSeat.getPrice()
                            );

                            bookingSeat.setCreatedAt(
                                    LocalDateTime.now()
                            );

                            return bookingSeat;
                        })
                        .toList();

        bookingSeatRepository.saveAll(bookingSeats);

        return bookingMapper.toResponse(
                savedBooking,
                bookingSeats
        );
    }

    @Override
    @Transactional(readOnly = true)
    public BookingResponse getBookingById(Long id) {

        Booking booking =
                bookingRepository.findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Booking not found with id: "
                                                + id
                                )
                        );

        List<BookingSeat> bookingSeats =
                bookingSeatRepository.findByBookingId(id);

        return bookingMapper.toResponse(
                booking,
                bookingSeats
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponse> getMyBookings() {

        /*
         * This will be implemented using the authenticated
         * user's ID once Spring Security authentication
         * is connected to the booking module.
         */

        return List.of();
    }
}