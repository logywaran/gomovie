package com.gomovie.showseat;

import com.gomovie.common.entity.BaseEntity;
import com.gomovie.screen.Screen;
import com.gomovie.seat.Seat;
import com.gomovie.show.MovieShow;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(
        name = "show_seat",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_show_seat",
                        columnNames = {"show_id", "seat_id"}
                ),
                @UniqueConstraint(
                        name = "uk_show_seat_id_show",
                        columnNames = {"id", "show_id"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
public class ShowSeat extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "show_id", nullable = false)
    private Long showId;

    @Column(name = "screen_id", nullable = false)
    private Long screenId;

    @Column(name = "seat_id", nullable = false)
    private Long seatId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "show_id",
            insertable = false,
            updatable = false
    )
    private MovieShow show;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "screen_id",
            insertable = false,
            updatable = false
    )
    private Screen screen;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "seat_id",
            insertable = false,
            updatable = false
    )
    private Seat seat;

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 20
    )
    private ShowSeatStatus status;

    @Column(
            nullable = false,
            precision = 10,
            scale = 2
    )
    private BigDecimal price;
}