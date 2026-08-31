package com.gomovie.ticket;

import java.time.LocalDateTime;

public record TicketResponse(

        Long id,

        Long bookingId,

        Long bookingSeatId,

        String ticketNumber,

        String qrToken,

        LocalDateTime issuedAt

) {
}