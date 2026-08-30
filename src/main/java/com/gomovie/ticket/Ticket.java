package com.gomovie.ticket;

import com.gomovie.bookingseat.BookingSeat;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "ticket",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_ticket_booking_seat",
                        columnNames = "booking_seat_id"
                ),
                @UniqueConstraint(
                        name = "uk_ticket_number",
                        columnNames = "ticket_number"
                ),
                @UniqueConstraint(
                        name = "uk_ticket_qr_token",
                        columnNames = "qr_token"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "booking_seat_id", nullable = false)
    private Long bookingSeatId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "booking_seat_id",
            insertable = false,
            updatable = false
    )
    private BookingSeat bookingSeat;

    @Column(
            name = "ticket_number",
            nullable = false,
            unique = true,
            length = 100
    )
    private String ticketNumber;

    @Column(
            name = "qr_token",
            nullable = false,
            unique = true,
            length = 255
    )
    private String qrToken;

    @Column(name = "issued_at", nullable = false)
    private LocalDateTime issuedAt;
}