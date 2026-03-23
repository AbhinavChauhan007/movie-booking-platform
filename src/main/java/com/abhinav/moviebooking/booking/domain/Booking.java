package com.abhinav.moviebooking.booking.domain;

import com.abhinav.moviebooking.booking.cancellation.BookingCancellationReason;
import com.abhinav.moviebooking.booking.lifecycle.BookingLifecycle;
import com.abhinav.moviebooking.booking.workflow.BookingExecutionContext;

import java.io.Serializable;
import java.time.Instant;

public class Booking implements Serializable {

    private Long bookingId;
    private Long userId;
    private BookingStatus bookingStatus;
    private Instant createdAt;
    private BookingCancellationReason cancellationReason;

    private BookingExecutionContext bookingExecutionContext;

    public Booking() {
    }

    public Booking(Long bookingId, Long userId, BookingStatus bookingStatus, Instant createdAt, BookingCancellationReason cancellationReason) {
        this.bookingId = bookingId;
        this.userId = userId;
        this.bookingStatus = bookingStatus;
        this.createdAt = createdAt;
        this.cancellationReason = cancellationReason;
    }

    /**
     * Factory method for NEW bookings only
     */
    public static Booking newBooking(Long userId) {
        return new Booking(
                null,
                userId,
                BookingStatus.CREATED,
                Instant.now(),
                null

        );
    }

    /**
     * Factory for rehydration from persistence
     */
    public static Booking rehydrate(Long bookingId, Long userId, BookingStatus status, Instant createdAt, BookingCancellationReason reason) {
        if (createdAt == null) {
            throw new IllegalStateException("createdAt cannot be null while rehydrating Booking");
        }
        if (userId == null) {
            throw new IllegalStateException("userId cannot be null while rehydrating Booking");
        }
        return new Booking(bookingId, userId, status, createdAt, reason);
    }

    // =====================
    // Domain behavior
    // =====================

    public void cancel(BookingCancellationReason reason) {
        if (bookingStatus.isFinal()) return;

        this.cancellationReason = reason;
        this.bookingStatus =
                (reason == BookingCancellationReason.EXPIRED)
                        ? BookingStatus.EXPIRED
                        : BookingStatus.CANCELLED;
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

    public BookingCancellationReason getCancellationReason() {
        return cancellationReason;
    }

    public Long getUserId() {  // NEW GETTER
        return userId;
    }

    public void transitionTo(BookingStatus targetStatus) {
        BookingLifecycle.validTransition(this.bookingStatus, targetStatus);
        this.bookingStatus = targetStatus;
    }
}
