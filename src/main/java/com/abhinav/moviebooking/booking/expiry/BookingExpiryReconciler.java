package com.abhinav.moviebooking.booking.expiry;

import com.abhinav.moviebooking.booking.cancellation.BookingCancellationReason;
import com.abhinav.moviebooking.booking.cancellation.BookingCancellationService;
import com.abhinav.moviebooking.booking.domain.Booking;
import com.abhinav.moviebooking.booking.domain.BookingStatus;
import com.abhinav.moviebooking.booking.persistence.adapter.BookingPersistenceAdapter;
import com.abhinav.moviebooking.booking.seat.core.SeatService;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Component
public class BookingExpiryReconciler {

    private static final Duration BOOKING_EXPIRY_DURATION = Duration.ofMinutes(10);

    private final BookingPersistenceAdapter bookingPersistenceAdapter;
    private final BookingCancellationService bookingCancellationService;

    public BookingExpiryReconciler(BookingPersistenceAdapter bookingPersistenceAdapter, BookingCancellationService bookingCancellationService) {
        this.bookingPersistenceAdapter = bookingPersistenceAdapter;

        this.bookingCancellationService = bookingCancellationService;
    }

    public void reconcileExpiredBookings() {
        Instant expiryThreshold = Instant.now().minus(BOOKING_EXPIRY_DURATION);
        int maxBatches = 10; // Production safety limit per run

        for (int i = 0; i < maxBatches; i++) {
            List<Long> expiredBookings =
                    bookingPersistenceAdapter.findExpiredInitiatedBookings(expiryThreshold, 50);

            if (expiredBookings.isEmpty()) break;

            for (Long bookingId : expiredBookings) {
                expireBookingSafely(bookingId);
            }
        }
    }


    public void expireBookingSafely(Long bookingId) {

        bookingCancellationService.cancelBooking(
                bookingId,
                BookingCancellationReason.EXPIRED
        );


    }
}
