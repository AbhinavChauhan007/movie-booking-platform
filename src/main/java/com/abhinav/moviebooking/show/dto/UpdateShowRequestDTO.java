package com.abhinav.moviebooking.show.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.Instant;

public record UpdateShowRequestDTO(
        @NotNull(message = "Start time is required")
        @Future(message = "Start time must be in the future")
        Instant startTime,

        @NotNull(message = "Screen number is required")
        @Positive(message = "Screen number must be positive")
        Integer screenNumber
) {
}
