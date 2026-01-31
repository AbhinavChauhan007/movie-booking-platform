package com.abhinav.moviebooking.show.dto;

import java.time.Instant;

public record CreateShowRequest(
        Long movieId,
        Instant startTime,
        Integer screenNumber,
        Integer totalSeats
) {}
