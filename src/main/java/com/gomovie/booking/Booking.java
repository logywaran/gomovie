package com.gomovie.booking;

import com.gomovie.common.entity.BaseEntity;
import com.gomovie.show.MovieShow;
import com.gomovie.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "booking",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_booking_reference",
                        columnNames = "booking_reference"
                ),
                @UniqueConstraint(
                        name = "uk_booking_id_show",
                        columnNames = {"id", "show_id"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
public class Booking extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "booking_reference",
            nullable = false,
            unique = true,
            length = 50
    )
    private String bookingReference;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "user_id",
            nullable = false
    )
    private User user;

    @Column(name = "show_id", nullable = false)
    private Long showId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "show_id",
            insertable = false,
            updatable = false
    )
    private MovieShow show;

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 20
    )
    private BookingStatus status;

    @Column(
            name = "total_amount",
            nullable = false,
            precision = 10,
            scale = 2
    )
    private BigDecimal totalAmount;

    @Column(name = "hold_expires_at")
    private LocalDateTime holdExpiresAt;
}