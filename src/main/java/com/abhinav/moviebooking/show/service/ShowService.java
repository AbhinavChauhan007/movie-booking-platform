package com.abhinav.moviebooking.show.service;

import com.abhinav.moviebooking.booking.persistence.repository.BookingRepository;
import com.abhinav.moviebooking.booking.seat.core.SeatService;
import com.abhinav.moviebooking.movie.exception.MovieNotFoundException;
import com.abhinav.moviebooking.movie.repository.MovieRepository;
import com.abhinav.moviebooking.show.dto.CreateShowRequest;
import com.abhinav.moviebooking.show.dto.UpdateShowRequestDTO;
import com.abhinav.moviebooking.show.entity.Show;
import com.abhinav.moviebooking.show.exception.ShowAlreadyBookedException;
import com.abhinav.moviebooking.show.exception.ShowNotFoundException;
import com.abhinav.moviebooking.show.exception.ShowValidationException;
import com.abhinav.moviebooking.show.repository.ShowRepository;
import com.abhinav.moviebooking.util.ErrorCode;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class ShowService {

    private final ShowRepository showRepository;
    private final SeatService seatService;
    private final MovieRepository movieRepository;
    private final BookingRepository bookingRepository;

    public ShowService(ShowRepository showRepository, SeatService seatService, MovieRepository movieRepository, BookingRepository bookingRepository) {
        this.showRepository = showRepository;
        this.seatService = seatService;
        this.movieRepository = movieRepository;
        this.bookingRepository = bookingRepository;
    }

    @Transactional
    public Show createShow(CreateShowRequest request) {
        if (movieRepository.findByIdAndActiveTrue(request.movieId()).isEmpty())
            throw new MovieNotFoundException(request.movieId());

        Show show = showRepository.save(
                new Show(
                        request.movieId(),
                        request.startTime(),
                        request.screenNumber(),
                        request.totalSeats()
                )
        );

        // Redis seat initialization
        seatService.initializeSeats(show.getId(), show.getTotalSeats());

        return show;
    }

    public Show getShowById(Long id) {
        return showRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ShowNotFoundException(id.toString()));
    }

    public java.util.List<Show> getAllActiveShows() {
        return showRepository.findAllByActiveTrue();
    }

    public java.util.List<Show> getFutureShows() {
        return showRepository.findAllFutureShows(Instant.now());
    }

    @Transactional
    public Show updateShow(Long id, UpdateShowRequestDTO request) {
        // 1. Fetch the show
        Show show = showRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ShowNotFoundException(id.toString()));

        // 2. Check if show has any active bookings
        if (bookingRepository.existsActiveBookingsByShowId(id)) {
            throw new ShowAlreadyBookedException(
                    "Cannot update show with ID " + id +
                            ". Show has existing bookings. Please cancel the show and create a new one."
            );
        }

        // 3. Validate new start time is in the future
        if (request.startTime().isBefore(Instant.now())) {
            throw new ShowValidationException(
                    ErrorCode.PAST_SHOW_BOOKING,
                    "Cannot update show to past time: " + request.startTime()
            );
        }

        // 4. Update the show (safe - no bookings exist)
        show.updateStartTime(request.startTime());
        show.updateScreenNumber(request.screenNumber());

        // 5. Save and return
        return showRepository.save(show);
    }

    @Transactional
    public void deleteShow(Long id) {
        Show show = showRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ShowNotFoundException(id.toString()));
        show.deactivate();
        showRepository.save(show);
    }
}
