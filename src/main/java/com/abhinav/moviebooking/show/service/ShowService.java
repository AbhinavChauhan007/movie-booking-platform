package com.abhinav.moviebooking.show.service;

import com.abhinav.moviebooking.booking.seat.core.SeatService;
import com.abhinav.moviebooking.show.dto.CreateShowRequest;
import com.abhinav.moviebooking.show.entity.Show;
import com.abhinav.moviebooking.show.repository.ShowRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class ShowService {

    private final ShowRepository showRepository;
    private final SeatService seatService;

    public ShowService(ShowRepository showRepository, SeatService seatService) {
        this.showRepository = showRepository;
        this.seatService = seatService;
    }

    @Transactional
    public Show createShow(CreateShowRequest request) {

        if (request.startTime().isBefore(Instant.now())) {
            throw new IllegalArgumentException("Show start time must be in the future");
        }

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
}
