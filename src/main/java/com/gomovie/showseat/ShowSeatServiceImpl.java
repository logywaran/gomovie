package com.gomovie.showseat;

import com.gomovie.common.exception.ResourceAlreadyExistsException;
import com.gomovie.common.exception.ResourceNotFoundException;
import com.gomovie.seat.Seat;
import com.gomovie.seat.SeatRepository;
import com.gomovie.show.MovieShow;
import com.gomovie.show.MovieShowRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ShowSeatServiceImpl implements ShowSeatService {

    private final ShowSeatRepository showSeatRepository;
    private final MovieShowRepository movieShowRepository;
    private final SeatRepository seatRepository;
    private final ShowSeatMapper showSeatMapper;

    @Override
    public List<ShowSeatResponse> generateShowSeats(Long showId) {

        // A ShowSeat can only be generated for an existing show.
        MovieShow movieShow =
                movieShowRepository.findById(showId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Show not found with id: " + showId
                                )
                        );

        // Only active physical seats of the show's screen
        // should become bookable inventory.
        List<Seat> seats =
                seatRepository.findByScreenIdAndIsActiveTrue(
                        movieShow.getScreen().getId()
                );

        if (seats.isEmpty()) {
            throw new ResourceNotFoundException(
                    "No seats found for screen: "
                            + movieShow.getScreen().getId()
            );
        }

        // Prevent generating duplicate ShowSeat records
        // for the same show.
        if (showSeatRepository.existsByShowId(showId)) {
            throw new ResourceAlreadyExistsException(
                    "Show seats already exist for show: " + showId
            );
        }

        List<ShowSeat> showSeats =
                seats.stream()
                        .map(seat -> {

                            ShowSeat showSeat = new ShowSeat();

                            showSeat.setShowId(showId);
                            showSeat.setScreenId(
                                    movieShow.getScreen().getId()
                            );
                            showSeat.setSeatId(seat.getId());

                            showSeat.setShow(movieShow);
                            showSeat.setScreen(movieShow.getScreen());
                            showSeat.setSeat(seat);

                            // Every newly generated seat starts
                            // as available for booking.
                            showSeat.setStatus(
                                    ShowSeatStatus.AVAILABLE
                            );

                            // Current MVP uses a fixed seat price.
                            showSeat.setPrice(
                                    BigDecimal.valueOf(200)
                            );

                            return showSeat;
                        })
                        .toList();

        List<ShowSeat> savedShowSeats =
                showSeatRepository.saveAll(showSeats);

        return savedShowSeats.stream()
                .map(showSeatMapper::toResponse)
                .toList();
    }

    @Override
    public List<ShowSeatResponse> getShowSeats(Long showId) {

        MovieShow movieShow =
                movieShowRepository.findById(showId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Show not found with id: " + showId
                                )
                        );

        // Customers should only see seats for active shows.
        if (!movieShow.getIsActive()) {
            throw new ResourceNotFoundException(
                    "Show not found with id: " + showId
            );
        }

        return showSeatRepository.findByShowId(showId)
                .stream()
                .map(showSeatMapper::toResponse)
                .toList();
    }
}