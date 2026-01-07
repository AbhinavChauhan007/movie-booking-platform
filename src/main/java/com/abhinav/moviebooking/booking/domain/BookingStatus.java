package com.abhinav.moviebooking.booking.domain;

public enum BookingStatus {

    CREATED,            // Booking created
    INITIATED,          // seats locked
    CONFIRMED,          // Payment successful
    CANCELLED,          // User/system cancelled
    EXPIRED;            // Lock/payment timeout

    public boolean isFinal() {
        return this == EXPIRED || this == CANCELLED;
    }

}
