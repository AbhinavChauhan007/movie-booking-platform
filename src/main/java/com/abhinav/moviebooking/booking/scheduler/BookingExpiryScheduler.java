package com.abhinav.moviebooking.booking.scheduler;

import com.abhinav.moviebooking.booking.domain.Booking;
import com.abhinav.moviebooking.booking.domain.BookingStatus;
import com.abhinav.moviebooking.booking.store.InMemoryBookingStore;
import com.abhinav.moviebooking.booking.workflow.impl.StandardBookingWorkflow;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class BookingExpiryScheduler {

    private static final long EXPIRY_DURATION_MS = 60_000; // 1 minute

    private final InMemoryBookingStore bookingStore;
    private final StandardBookingWorkflow standardBookingWorkflow;

    public BookingExpiryScheduler(InMemoryBookingStore bookingStore, StandardBookingWorkflow standardBookingWorkflow) {
        this.bookingStore = bookingStore;
        this.standardBookingWorkflow = standardBookingWorkflow;
    }

    /**
     * Periodically scans INITIATED bookings and expires timed-out ones.
     */
    @Scheduled(fixedDelay = 10_000)
    public void expireOldBookings() {
        long now = System.currentTimeMillis();

        for (Booking booking : bookingStore.findAll()) {
            if (booking.getBookingStatus() == BookingStatus.INITIATED &&
                    now - booking.getCreatedAt() > EXPIRY_DURATION_MS)
                standardBookingWorkflow.expireBooking(booking);
        }
    }
}
