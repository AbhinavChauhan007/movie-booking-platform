package com.abhinav.moviebooking.booking.domain;

public enum BookingStatus {

    INITIATED,          // Booking created, seats not locked yet
    SEATS_LOCKED,       // Seats locked temporarily
    PENDING_PAYMENT,    // Waiting for payment
    CONFIRMED,          // Payment successful
    CANCELLED,          // User/system cancelled
    EXPIRED;            // Lock/payment timeout

    public boolean isFinal() {
        return this == CONFIRMED || this == EXPIRED || this == CANCELLED;
    }

}
