package com.abhinav.moviebooking.booking.scheduler;

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

    private static final long EXPIRY_DURATION_MS = 60_000; // 1 minute

    private final StandardBookingWorkflow standardBookingWorkflow;
    private final BookingPersistenceAdapter bookingPersistenceAdapter;
    private final BookingCache bookingCache;

    public BookingExpiryScheduler(StandardBookingWorkflow standardBookingWorkflow, BookingPersistenceAdapter bookingPersistenceAdapter, BookingCache bookingCache) {
        this.bookingPersistenceAdapter = bookingPersistenceAdapter;
        this.bookingCache = bookingCache;
        this.standardBookingWorkflow = standardBookingWorkflow;
    }

    /**
     * Periodically scans INITIATED bookings and expires timed-out ones.
     */
    @Scheduled(fixedDelay = 10_000)
    public void expireOldBookings() {
        Instant expiryThreshold = Instant.now().minusMillis(EXPIRY_DURATION_MS);

        List<Booking> expiredBookings = bookingPersistenceAdapter.findExpiredInitiatedBookings(expiryThreshold);

        for (Booking booking : expiredBookings) {
            try {
                standardBookingWorkflow.expireBooking(booking);
                bookingPersistenceAdapter.save(booking);
                bookingCache.put(booking);
            } catch (Exception e) {
                // optimistic lock failures are expected in concurrency
                System.out.println(e.getMessage());
            }
        }
    }
}
