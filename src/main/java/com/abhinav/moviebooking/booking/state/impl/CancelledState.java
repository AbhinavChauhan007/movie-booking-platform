package com.abhinav.moviebooking.booking.state.impl;

import com.abhinav.moviebooking.booking.state.BookingState;
import org.springframework.stereotype.Component;

@Component
public class CancelledState implements BookingState {

    @Override
    public void handle(Long bookingId) {
        System.out.println("Booking " + bookingId + " is CANCELLED");
        // release seats, refund payment
    }
}
