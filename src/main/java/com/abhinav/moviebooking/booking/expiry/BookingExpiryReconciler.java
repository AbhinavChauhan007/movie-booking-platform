package com.abhinav.moviebooking.booking.expiry;

import com.abhinav.moviebooking.booking.cancellation.BookingCancellationReason;
import com.abhinav.moviebooking.booking.cancellation.BookingCancellationService;
import com.abhinav.moviebooking.booking.domain.Booking;
import com.abhinav.moviebooking.booking.domain.BookingStatus;
import com.abhinav.moviebooking.booking.persistence.adapter.BookingPersistenceAdapter;
import com.abhinav.moviebooking.booking.seat.service.SeatService;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Component
public class BookingExpiryReconciler {

    private static final Duration BOOKING_EXPIRY_DURATION = Duration.ofMinutes(10);

    private final BookingPersistenceAdapter bookingPersistenceAdapter;
    private final SeatService seatService;
    private final BookingCancellationService bookingCancellationService;

    public BookingExpiryReconciler(BookingPersistenceAdapter bookingPersistenceAdapter, SeatService seatService, BookingCancellationService bookingCancellationService) {
        this.bookingPersistenceAdapter = bookingPersistenceAdapter;
        this.seatService = seatService;
        this.bookingCancellationService = bookingCancellationService;
    }

    public void reconcileExpiredBookings() {
        Instant expiryThreshold = Instant.now().minus(BOOKING_EXPIRY_DURATION);

        List<Booking> expiredBookings =
                bookingPersistenceAdapter.findExpiredInitiatedBookings(expiryThreshold);

        for (Booking booking : expiredBookings)
            expireBookingSafely(booking);

    }

    private void expireBookingSafely(Booking booking) {
        // Idempotency guard
        if (booking.getBookingStatus() != BookingStatus.INITIATED)
            return;

        // Release Redis-locked seats (safe even if TTL already expired)
        if (booking.getBookingExecutionContext() != null && booking.getBookingExecutionContext().getAllocatedSeats() != null) {
            seatService.releaseSeats(
                    booking.getBookingExecutionContext().getShowId(),
                    booking.getBookingExecutionContext().getAllocatedSeats()
            );
        }

        bookingCancellationService.cancelBooking(
                booking.getBookingId(),
                BookingCancellationReason.EXPIRED
        );


    }
}
