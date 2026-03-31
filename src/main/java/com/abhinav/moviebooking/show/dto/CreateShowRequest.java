package com.abhinav.moviebooking.show.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record CreateShowRequest(

        @NotNull(message = "Movie Id is required")
        Long movieId,

        @NotNull(message = "start time is required")
        @Future(message = "Show start time must be in future")
        Instant startTime,

        @NotNull(message = "Screen Number is required")
        @Min(value = 1, message = "Screen number must be at least 1")
        Integer screenNumber,

        @NotNull(message = "Total seats is required")
        @Min(value = 1, message = "Total seats must be at least 1")
        Integer totalSeats
) {}
