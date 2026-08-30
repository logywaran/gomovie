package com.gomovie.bookingseat;

import com.gomovie.booking.Booking;
import com.gomovie.showseat.ShowSeat;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "booking_seat",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_booking_seat_id_show",
                        columnNames = {"id", "show_id"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
public class BookingSeat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "booking_id", nullable = false)
    private Long bookingId;

    @Column(name = "show_id", nullable = false)
    private Long showId;

    @Column(name = "show_seat_id", nullable = false)
    private Long showSeatId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumns({
            @JoinColumn(
                    name = "booking_id",
                    referencedColumnName = "id",
                    insertable = false,
                    updatable = false
            ),
            @JoinColumn(
                    name = "show_id",
                    referencedColumnName = "show_id",
                    insertable = false,
                    updatable = false
            )
    })
    private Booking booking;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumns({
            @JoinColumn(
                    name = "show_seat_id",
                    referencedColumnName = "id",
                    insertable = false,
                    updatable = false
            ),
            @JoinColumn(
                    name = "show_id",
                    referencedColumnName = "show_id",
                    insertable = false,
                    updatable = false
            )
    })
    private ShowSeat showSeat;

    @Column(
            nullable = false,
            precision = 10,
            scale = 2
    )
    private BigDecimal price;

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private LocalDateTime createdAt;
}