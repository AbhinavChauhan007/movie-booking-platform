package com.abhinav.moviebooking.booking.exception;

public class BookingConcurrencyException extends RuntimeException {
    public BookingConcurrencyException(Long bookingId) {
        super("Booking was modified concurrently. Please retry. BookingId=" + bookingId);
    }
}
