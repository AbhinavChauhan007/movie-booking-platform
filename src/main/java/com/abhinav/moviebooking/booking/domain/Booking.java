package com.abhinav.moviebooking.booking.domain;

import com.abhinav.moviebooking.booking.lifecycle.BookingLifecycle;

public class Booking {

    private Long id;
    private BookingStatus status;

    public Booking(Long id, BookingStatus status) {
        this.id = id;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public void transitionTo(BookingStatus targetStatus) {
        BookingLifecycle.validTransition(this.status, targetStatus);
        this.status = targetStatus;
    }
}
