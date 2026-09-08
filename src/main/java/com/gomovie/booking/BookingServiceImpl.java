package com.gomovie.booking;

import com.gomovie.bookingseat.BookingSeat;
import com.gomovie.bookingseat.BookingSeatRepository;
import com.gomovie.common.exception.*;
import com.gomovie.show.MovieShow;
import com.gomovie.show.MovieShowRepository;
import com.gomovie.showseat.ShowSeat;
import com.gomovie.showseat.ShowSeatRepository;
import com.gomovie.showseat.ShowSeatStatus;
import com.gomovie.user.User;
import com.gomovie.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

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
    public BookingResponse createBooking(BookingRequest request) {

        Set<Long> uniqueSeatIds =
                new HashSet<>(request.showSeatIds());

        if (uniqueSeatIds.size() != request.showSeatIds().size()) {
            throw new InvalidRequestException(
                    "Duplicate seat IDs are not allowed"
            );
        }

        MovieShow show = movieShowRepository.findById(request.showId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Show not found with id: " + request.showId()));

        if (!show.getIsActive()) {
            throw new InvalidStateException("Show is not active");
        }

        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Authenticated user not found"));

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

            totalAmount = totalAmount.add(
                    showSeat.getPrice()
            );
        }

        showSeatRepository.saveAll(showSeats);

        Booking booking = new Booking();

        booking.setBookingReference(
                "GM-" +
                        UUID.randomUUID()
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

        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Booking not found with id: " + id
                ));

        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Authenticated user not found"
                ));

        if (!booking.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException(
                    "You are not allowed to access this booking"
            );
        }

        List<BookingSeat> bookingSeats =
                bookingSeatRepository.findByBookingId(id);

        return bookingMapper.toResponse(booking, bookingSeats);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponse> getMyBookings() {

        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "Authenticated user not found"
                        )
                );

        List<Booking> bookings =
                bookingRepository.findByUserId(user.getId());

        List<Long> bookingIds = bookings.stream()
                .map(Booking::getId)
                .toList();

        List<BookingSeat> bookingSeats =
                bookingSeatRepository.findByBookingIdIn(bookingIds);

        Map<Long, List<BookingSeat>> seatsByBookingId =
                bookingSeats.stream()
                        .collect(Collectors.groupingBy(
                                BookingSeat::getBookingId
                        ));

        return bookings.stream()
                .map(booking -> {

                    List<BookingSeat> seats =
                            seatsByBookingId.getOrDefault(
                                    booking.getId(),
                                    List.of()
                            );

                    return bookingMapper.toResponse(
                            booking,
                            seats
                    );
                })
                .toList();
    }
}