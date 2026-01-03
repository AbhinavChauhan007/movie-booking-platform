package com.abhinav.moviebooking.booking.state;

public interface BookingState {

    void handle(Long bookingId);
}
