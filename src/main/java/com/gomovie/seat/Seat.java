package com.gomovie.seat;

import com.gomovie.common.entity.BaseEntity;
import com.gomovie.screen.Screen;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "seat",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_seat_screen_row_number",
                        columnNames = {
                                "screen_id",
                                "row_label",
                                "seat_number"
                        }
                ),
                @UniqueConstraint(
                        name = "uk_seat_id_screen",
                        columnNames = {
                                "id",
                                "screen_id"
                        }
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
public class Seat extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "screen_id",
            nullable = false
    )
    private Screen screen;

    @Column(name = "row_label", nullable = false, length = 10)
    private String rowLabel;

    @Column(name = "seat_number", nullable = false)
    private Integer seatNumber;

    @Column(name = "seat_type", nullable = false, length = 50)
    private String seatType;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}