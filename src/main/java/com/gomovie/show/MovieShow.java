package com.gomovie.show;

import com.gomovie.movie.Movie;
import com.gomovie.screen.Screen;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(
        name = "movie_show"
//        ,
//        uniqueConstraints = {
//                @UniqueConstraint(
//                        name = "uk_movie_show_id_screen",
//                        columnNames = {"id", "screen_id"}
//                )
//        }
)
@Getter
@Setter
@NoArgsConstructor
public class MovieShow {// 1 movie --* movie show *-- 1 Screen can host many shows

    //Movie + screen + data + time = MovieShow
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "movie_id", nullable = false)
    private Movie movie;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "screen_id", nullable = false)
    private Screen screen;

    @Column(name = "show_date", nullable = false)
    private LocalDate showDate;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private MovieLanguage language;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
}