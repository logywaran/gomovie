package com.gomovie.show;

import com.gomovie.movie.Movie;
import com.gomovie.screen.Screen;
import org.springframework.stereotype.Component;

@Component
public class MovieShowMapper {

    public MovieShowResponse toResponse(MovieShow movieShow) {

        return new MovieShowResponse(
                movieShow.getId(),
                movieShow.getMovie().getId(),
                movieShow.getScreen().getId(),
                movieShow.getShowDate(),
                movieShow.getStartTime(),
                movieShow.getEndTime(),
                movieShow.getIsActive()
        );
    }

    public MovieShow toEntity(
            MovieShowRequest request,
            Movie movie,
            Screen screen) {

        MovieShow movieShow = new MovieShow();

        movieShow.setMovie(movie);
        movieShow.setScreen(screen);
        movieShow.setShowDate(request.showDate());
        movieShow.setStartTime(request.startTime());
        movieShow.setEndTime(request.endTime());
        movieShow.setIsActive(true);

        return movieShow;
    }

    public void updateEntity(
            MovieShow movieShow,
            MovieShowRequest request,
            Movie movie,
            Screen screen) {

        movieShow.setMovie(movie);
        movieShow.setScreen(screen);
        movieShow.setShowDate(request.showDate());
        movieShow.setStartTime(request.startTime());
        movieShow.setEndTime(request.endTime());
    }
}