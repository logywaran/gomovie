package com.gomovie.ticket;

import com.gomovie.booking.Booking;
import com.gomovie.booking.BookingRepository;
import com.gomovie.booking.BookingStatus;
import com.gomovie.bookingseat.BookingSeat;
import com.gomovie.bookingseat.BookingSeatRepository;
import com.gomovie.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TicketServiceImpl implements TicketService {

    private final BookingRepository bookingRepository;
    private final BookingSeatRepository bookingSeatRepository;
    private final TicketRepository ticketRepository;
    private final TicketMapper ticketMapper;

    @Override
    @Transactional
    public List<TicketResponse> generateTickets(Long bookingId) {

        Booking booking =
                bookingRepository.findById(bookingId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Booking not found with id: " + bookingId
                                )
                        );

        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new IllegalStateException(
                    "Tickets can only be generated for a confirmed booking"
            );
        }

        List<BookingSeat> bookingSeats =
                bookingSeatRepository.findByBookingId(bookingId);

        return bookingSeats.stream()
                .map(bookingSeat -> {

                    if (ticketRepository.existsByBookingSeatId(
                            bookingSeat.getId())) {

                        return ticketRepository
                                .findAll()
                                .stream()
                                .filter(ticket ->
                                        ticket.getBookingSeatId()
                                                .equals(bookingSeat.getId()))
                                .findFirst()
                                .orElseThrow();
                    }

                    Ticket ticket = new Ticket();

                    ticket.setBookingSeatId(
                            bookingSeat.getId()
                    );

                    ticket.setBookingSeat(
                            bookingSeat
                    );

                    ticket.setTicketNumber(
                            "GM-TKT-" +
                                    UUID.randomUUID()
                                            .toString()
                                            .replace("-", "")
                                            .substring(0, 12)
                                            .toUpperCase()
                    );

                    ticket.setQrToken(
                            UUID.randomUUID().toString()
                    );

                    ticket.setIssuedAt(
                            LocalDateTime.now()
                    );

                    return ticketRepository.save(ticket);
                })
                .map(ticketMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TicketResponse> getTicketsByBooking(Long bookingId) {

        if (!bookingRepository.existsById(bookingId)) {
            throw new ResourceNotFoundException(
                    "Booking not found with id: " + bookingId
            );
        }

        return ticketRepository.findAll()
                .stream()
                .filter(ticket ->
                        ticket.getBookingSeat()
                                .getBookingId()
                                .equals(bookingId)
                )
                .map(ticketMapper::toResponse)
                .toList();
    }
}