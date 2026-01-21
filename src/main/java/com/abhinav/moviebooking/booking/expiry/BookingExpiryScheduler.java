package com.abhinav.moviebooking.booking.expiry;

import com.abhinav.moviebooking.booking.cache.BookingCache;
import com.abhinav.moviebooking.booking.domain.Booking;
import com.abhinav.moviebooking.booking.persistence.adapter.BookingPersistenceAdapter;
import com.abhinav.moviebooking.booking.workflow.impl.StandardBookingWorkflow;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
public class BookingExpiryScheduler {

    private static final long EXPIRY_DURATION_MS = 3_00_000; // 1 minute

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
