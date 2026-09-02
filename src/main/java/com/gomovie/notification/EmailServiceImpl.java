package com.gomovie.notification;

import com.gomovie.booking.Booking;
import com.gomovie.booking.BookingRepository;
import com.gomovie.bookingseat.BookingSeat;
import com.gomovie.bookingseat.BookingSeatRepository;
import com.gomovie.common.exception.ResourceNotFoundException;
import com.gomovie.seat.Seat;
import com.gomovie.show.MovieShow;
import com.gomovie.showseat.ShowSeat;
import com.gomovie.ticket.Ticket;
import com.gomovie.ticket.TicketRepository;
import com.gomovie.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final BookingRepository bookingRepository;
    private final BookingSeatRepository bookingSeatRepository;
    private final TicketRepository ticketRepository;

    @Value("${spring.mail.username}")
    private String mailUsername;

    @Override
    @Transactional(readOnly = true)
    public void sendBookingConfirmation(Long bookingId) {

        Booking booking =
                bookingRepository.findById(bookingId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Booking not found with id: " + bookingId
                                )
                        );

        User user = booking.getUser();
        MovieShow show = booking.getShow();

        String movieTitle = show.getMovie().getTitle();
        String screenName = show.getScreen().getName();
        String theatreName = show.getScreen().getTheatre().getName();
        String theatreAddress = show.getScreen().getTheatre().getAddress();
        String cityName = show.getScreen().getTheatre().getCity().getName();

        List<BookingSeat> bookingSeats =
                bookingSeatRepository.findByBookingId(bookingId);

        StringBuilder ticketDetails = new StringBuilder();

        for (BookingSeat bookingSeat : bookingSeats) {

            Ticket ticket =
                    ticketRepository
                            .findByBookingSeatId(bookingSeat.getId())
                            .orElseThrow(() ->
                                    new ResourceNotFoundException(
                                            "Ticket not found for booking seat: "
                                                    + bookingSeat.getId()
                                    )
                            );

            ShowSeat showSeat = bookingSeat.getShowSeat();
            Seat seat = showSeat.getSeat();

            ticketDetails
                    .append("Ticket Number: ")
                    .append(ticket.getTicketNumber())
                    .append("\n")
                    .append("Seat: ")
                    .append(seat.getRowLabel())
                    .append(seat.getSeatNumber())
                    .append("\n")
                    .append("Seat Type: ")
                    .append(seat.getSeatType())
                    .append("\n")
                    .append("Price: RM ")
                    .append(bookingSeat.getPrice())
                    .append("\n")
                    .append("QR Token: ")
                    .append(ticket.getQrToken())
                    .append("\n\n");
        }

        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom(mailUsername);
        message.setTo(user.getEmail());

        message.setSubject(
                "GoMovie Booking Confirmation - "
                        + booking.getBookingReference()
        );

        message.setText(
                """
                Hi %s,

                Your GoMovie booking has been confirmed successfully!

                BOOKING DETAILS
                ----------------
                Booking Reference: %s

                MOVIE
                ----------------
                Movie: %s

                THEATRE
                ----------------
                Theatre: %s
                Address: %s
                City: %s

                SCREEN
                ----------------
                Screen: %s

                SHOW
                ----------------
                Date: %s
                Time: %s - %s

                SEAT DETAILS
                ----------------
                %s
                PAYMENT
                ----------------
                Payment Method: FPX
                Total Amount: RM %s

                Thank you for booking with GoMovie.

                Regards,
                GoMovie Team
                """.formatted(
                        user.getName(),
                        booking.getBookingReference(),
                        movieTitle,
                        theatreName,
                        theatreAddress,
                        cityName,
                        screenName,
                        show.getShowDate(),
                        show.getStartTime(),
                        show.getEndTime(),
                        ticketDetails,
                        booking.getTotalAmount()
                )
        );

        mailSender.send(message);
    }
}