package com.abhinav.moviebooking.booking.expiry;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class BookingExpiryScheduler {

    private final BookingExpiryReconciler bookingExpiryReconciler;

    public BookingExpiryScheduler(BookingExpiryReconciler bookingExpiryReconciler) {
        this.bookingExpiryReconciler = bookingExpiryReconciler;
    }


    /**
     * Periodically scans INITIATED bookings and expires timed-out ones.
     */
    @Scheduled(fixedDelay = 60_000)
    public void runExpiryReconciliation() {
        bookingExpiryReconciler.reconcileExpiredBookings();
    }
}
