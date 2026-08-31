package com.gomovie.ticket;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    Optional<Ticket> findByTicketNumber(String ticketNumber);

    Optional<Ticket> findByQrToken(String qrToken);

    boolean existsByBookingSeatId(Long bookingSeatId);

    Optional<Ticket> findByBookingSeatId(Long bookingSeatId);
}