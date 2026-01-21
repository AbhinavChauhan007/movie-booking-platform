package com.abhinav.moviebooking.booking.domain;

import com.abhinav.moviebooking.booking.lifecycle.BookingLifecycle;
import com.abhinav.moviebooking.booking.workflow.BookingExecutionContext;

import java.time.Instant;

public class Booking {

    private Long bookingId;
    private BookingStatus bookingStatus;
    private Instant createdAt;

    private BookingExecutionContext bookingExecutionContext;

    public Booking(Long bookingId, BookingStatus bookingStatus, Instant createdAt) {
        this.bookingId = bookingId;
        this.bookingStatus = bookingStatus;
        this.createdAt = createdAt;
    }

    /**
     * Factory method for NEW bookings only
     */
    public static Booking newBooking() {
        return new Booking(
                null,
                BookingStatus.CREATED,
                Instant.now()
        );
    }

    /**
     * Factory for rehydration from persistence
     */
    public static Booking rehydrate(Long bookingId, BookingStatus status, Instant createdAt) {
        if (createdAt == null) {
            throw new IllegalStateException("createdAt cannot be null while rehydrating Booking");
        }
        return new Booking(bookingId, status, createdAt);
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

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void transitionTo(BookingStatus targetStatus) {
        BookingLifecycle.validTransition(this.bookingStatus, targetStatus);
        this.bookingStatus = targetStatus;
    }
}
