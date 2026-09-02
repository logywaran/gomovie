package com.gomovie.payment;

import com.gomovie.booking.Booking;
import com.gomovie.booking.BookingRepository;
import com.gomovie.booking.BookingStatus;
import com.gomovie.bookingseat.BookingSeat;
import com.gomovie.bookingseat.BookingSeatRepository;
import com.gomovie.common.exception.ResourceAlreadyExistsException;
import com.gomovie.common.exception.ResourceNotFoundException;
import com.gomovie.notification.EmailService;
import com.gomovie.payment.provider.PaymentInitiationResult;
import com.gomovie.payment.provider.PaymentProvider;
import com.gomovie.showseat.ShowSeat;
import com.gomovie.showseat.ShowSeatRepository;
import com.gomovie.showseat.ShowSeatStatus;
import com.gomovie.ticket.TicketService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final PaymentMapper paymentMapper;
    private final PaymentProvider paymentProvider;
    private final BookingSeatRepository bookingSeatRepository;
    private final ShowSeatRepository showSeatRepository;
    private final TicketService ticketService;
    private final EmailService emailService;

    public PaymentServiceImpl(
            PaymentRepository paymentRepository,
            BookingRepository bookingRepository,
            PaymentMapper paymentMapper,
            @Qualifier("mobiFpxPaymentProvider")
            PaymentProvider paymentProvider,
            BookingSeatRepository bookingSeatRepository,
            ShowSeatRepository showSeatRepository,
            TicketService ticketService,
            EmailService emailService
    ) {
        this.paymentRepository = paymentRepository;
        this.bookingRepository = bookingRepository;
        this.paymentMapper = paymentMapper;
        this.paymentProvider = paymentProvider;
        this.bookingSeatRepository = bookingSeatRepository;
        this.showSeatRepository = showSeatRepository;
        this.ticketService = ticketService;
        this.emailService = emailService;
    }

    @Override
    @Transactional
    public PaymentResponse initiatePayment(PaymentRequest request) {

        log.info(
                "Initiating payment for bookingId={}",
                request.bookingId()
        );

        Booking booking =
                bookingRepository.findById(request.bookingId())
                        .orElseThrow(() -> {
                            log.warn(
                                    "Cannot initiate payment. Booking not found. bookingId={}",
                                    request.bookingId()
                            );

                            return new ResourceNotFoundException(
                                    "Booking not found with id: "
                                            + request.bookingId()
                            );
                        });

        if (paymentRepository.existsByBookingId(request.bookingId())) {

            log.warn(
                    "Payment already exists for bookingId={}",
                    request.bookingId()
            );

            throw new ResourceAlreadyExistsException(
                    "Payment already exists for booking: "
                            + request.bookingId()
            );
        }

        if (booking.getStatus() != BookingStatus.PENDING) {

            log.warn(
                    "Cannot initiate payment. Booking is not pending. " +
                            "bookingId={}, status={}",
                    request.bookingId(),
                    booking.getStatus()
            );

            throw new IllegalStateException(
                    "Payment can only be initiated for a pending booking"
            );
        }

        BigDecimal amount = booking.getTotalAmount();

        Payment payment = new Payment();

        payment.setBooking(booking);

        payment.setTransactionId(
                "GM-PAY-" + UUID.randomUUID()
                        .toString()
                        .replace("-", "")
                        .substring(0, 12)
                        .toUpperCase()
        );

        payment.setAmount(amount);

        payment.setStatus(PaymentStatus.PENDING);

        payment.setPaymentMethod(
                request.paymentMethod()
        );

        payment.setProvider("MOBI_FPX");

        PaymentInitiationResult result =
                paymentProvider.initiatePayment(
                        payment.getTransactionId(),
                        payment.getAmount()
                );

        payment.setPaymentUrl(result.paymentUrl());

        Payment savedPayment =
                paymentRepository.save(payment);

        log.info(
                "Payment initiated successfully. paymentId={}, bookingId={}, amount={}",
                savedPayment.getId(),
                request.bookingId(),
                amount
        );

        PaymentResponse response =
                paymentMapper.toResponse(savedPayment);

        return new PaymentResponse(
                response.id(),
                response.bookingId(),
                response.transactionId(),
                response.paymentUrl(),
                result.redirectHtml(),
                response.amount(),
                response.status(),
                response.paymentMethod(),
                response.provider(),
                response.failureReason(),
                response.createdAt()
        );
    }

    @Transactional
    public void handlePaymentSuccess(String transactionId) {

        Payment payment =
                paymentRepository.findByTransactionId(transactionId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Payment not found"
                                )
                        );

        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            return;
        }

        payment.setStatus(PaymentStatus.SUCCESS);

        Booking booking = payment.getBooking();

        List<BookingSeat> bookingSeats =
                bookingSeatRepository.findByBookingId(
                        booking.getId()
                );

        for (BookingSeat bookingSeat : bookingSeats) {

            ShowSeat showSeat =
                    showSeatRepository.findById(
                            bookingSeat.getShowSeatId()
                    ).orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "Show seat not found"
                            )
                    );

            if (showSeat.getStatus() != ShowSeatStatus.HELD) {
                throw new IllegalStateException(
                        "Show seat is no longer held"
                );
            }

            showSeat.setStatus(ShowSeatStatus.BOOKED);

            showSeatRepository.save(showSeat);
        }

        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setHoldExpiresAt(null);

        bookingRepository.save(booking);
        paymentRepository.save(payment);

        ticketService.generateTickets(booking.getId());

        emailService.sendBookingConfirmation(booking.getId());

        log.info(
                "Payment successful. bookingId={}, transactionId={}",
                booking.getId(),
                transactionId
        );
    }

    @Override
    @Transactional
    public void handlePaymentFailure(String transactionId) {

        Payment payment =
                paymentRepository.findByTransactionId(transactionId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Payment not found"
                                )
                        );

        if (payment.getStatus() == PaymentStatus.FAILED) {
            return;
        }

        payment.setStatus(PaymentStatus.FAILED);

        Booking booking = payment.getBooking();

        List<BookingSeat> bookingSeats =
                bookingSeatRepository.findByBookingId(
                        booking.getId()
                );

        for (BookingSeat bookingSeat : bookingSeats) {

            ShowSeat showSeat =
                    showSeatRepository.findById(
                            bookingSeat.getShowSeatId()
                    ).orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "Show seat not found"
                            )
                    );

            if (showSeat.getStatus() == ShowSeatStatus.HELD) {
                showSeat.setStatus(ShowSeatStatus.AVAILABLE);
                showSeatRepository.save(showSeat);
            }
        }

        booking.setStatus(BookingStatus.FAILED);
        booking.setHoldExpiresAt(null);

        bookingRepository.save(booking);
        paymentRepository.save(payment);

        log.info(
                "Payment failed. bookingId={}, transactionId={}",
                booking.getId(),
                transactionId
        );
    }
}