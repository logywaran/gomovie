package com.gomovie.ticket;

import java.util.List;

public interface TicketService {

    List<TicketResponse> generateTickets(Long bookingId);

    List<TicketResponse> getTicketsByBooking(Long bookingId);
}