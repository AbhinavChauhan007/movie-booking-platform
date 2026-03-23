package com.abhinav.moviebooking.event;

/**
 * Event published when a booking is cancelled.
 */
public class BookingCancelledEvent extends BookingEvent{
    private String cancellationReason;

    // Default constructor for JSON deserialization
    public BookingCancelledEvent() {
        super();
    }

    public BookingCancelledEvent(Long bookingId, Long userId, String cancellationReason) {
        super(bookingId, userId, "BOOKING_CANCELLED");
        this.cancellationReason = cancellationReason;
    }

    public String getCancellationReason() {
        return cancellationReason;
    }

    public void setCancellationReason(String cancellationReason) {
        this.cancellationReason = cancellationReason;
    }

    @Override
    public String toString() {
        return "BookingCancelledEvent{" +
                "cancellationReason='" + cancellationReason + '\'' +
                '}';
    }
}
