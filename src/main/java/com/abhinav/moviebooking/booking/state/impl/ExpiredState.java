package com.abhinav.moviebooking.booking.state.impl;

import com.abhinav.moviebooking.booking.state.BookingState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ExpiredState implements BookingState {

    private static final Logger log = LoggerFactory.getLogger(ExpiredState.class);

    @Override
    public void handle(Long bookingId) {
        log.info("Booking {} is EXPIRED", bookingId);
        // auto-release seats, cleanup
    }
}
