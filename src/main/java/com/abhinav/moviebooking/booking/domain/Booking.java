package com.abhinav.moviebooking.booking.domain;

import com.abhinav.moviebooking.booking.lifecycle.BookingLifecycle;
import com.abhinav.moviebooking.booking.workflow.BookingExecutionContext;

public class Booking {

    private final Long bookingId;
    private BookingStatus bookingStatus;
    private final long createdAt;

    private BookingExecutionContext bookingExecutionContext;

    public Booking(Long bookingId) {
        this.bookingId = bookingId;
        this.bookingStatus = BookingStatus.CREATED;
        this.createdAt = System.currentTimeMillis();
    }

    public void attachExecutionContext(BookingExecutionContext bookingExecutionContext) {
        this.bookingExecutionContext = bookingExecutionContext;
    }

    public BookingExecutionContext getBookingExecutionContext() {
        return bookingExecutionContext;
    }

    public Long getBookingId() {
        return bookingId;
    }

    public BookingStatus getBookingStatus() {
        return bookingStatus;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void transitionTo(BookingStatus targetStatus) {
        BookingLifecycle.validTransition(this.bookingStatus, targetStatus);
        this.bookingStatus = targetStatus;
    }
}
