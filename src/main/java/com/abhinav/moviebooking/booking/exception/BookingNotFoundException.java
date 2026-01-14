package com.abhinav.moviebooking.booking.exception;

public class BookingNotFoundException extends RuntimeException {
    public BookingNotFoundException(Long bookingId) {
        super("Booking with id " + bookingId + " not found");
    }
}
