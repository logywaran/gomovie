package com.gomovie.notification;

import com.gomovie.booking.Booking;
import com.gomovie.booking.BookingRepository;
import com.gomovie.bookingseat.BookingSeat;
import com.gomovie.bookingseat.BookingSeatRepository;
import com.gomovie.common.exception.ResourceNotFoundException;
import com.gomovie.ticket.Ticket;
import com.gomovie.ticket.TicketRepository;
import com.gomovie.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

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
    public void sendBookingConfirmation(Long bookingId) {

        Booking booking =
                bookingRepository.findById(bookingId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Booking not found with id: " + bookingId
                                )
                        );

        User user = booking.getUser();

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

            ticketDetails
                    .append("Ticket Number: ")
                    .append(ticket.getTicketNumber())
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

                Booking Reference: %s
                Show ID: %s
                Total Amount: RM %s

                Ticket Details:

                %s

                Thank you for booking with GoMovie.

                Regards,
                GoMovie Team
                """.formatted(
                        user.getName(),
                        booking.getBookingReference(),
                        booking.getShowId(),
                        booking.getTotalAmount(),
                        ticketDetails
                )
        );

        mailSender.send(message);
    }
}