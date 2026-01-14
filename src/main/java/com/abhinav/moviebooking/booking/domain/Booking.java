package com.abhinav.moviebooking.booking.domain;

import com.abhinav.moviebooking.booking.lifecycle.BookingLifecycle;
import com.abhinav.moviebooking.booking.workflow.BookingExecutionContext;

public class Booking {

    private Long bookingId;
    private BookingStatus bookingStatus;

    private BookingExecutionContext bookingExecutionContext;

    public Booking() {
        this.bookingStatus = BookingStatus.CREATED;
    }

    public Booking(Long bookingId, BookingStatus bookingStatus) {
        this.bookingId = bookingId;
        this.bookingStatus = bookingStatus;
    }

    public void assignId(Long bookingId) {
        this.bookingId = bookingId;
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


    public void transitionTo(BookingStatus targetStatus) {
        BookingLifecycle.validTransition(this.bookingStatus, targetStatus);
        this.bookingStatus = targetStatus;
    }
}
