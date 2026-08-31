package com.gomovie.ticket;

import org.springframework.stereotype.Component;

@Component
public class TicketMapper {

    public TicketResponse toResponse(Ticket ticket) {

        return new TicketResponse(
                ticket.getId(),
                ticket.getBookingSeat().getBookingId(),
                ticket.getBookingSeatId(),
                ticket.getTicketNumber(),
                ticket.getQrToken(),
                ticket.getIssuedAt()
        );
    }
}