package com.abhinav.moviebooking.booking.cancellation;

import com.abhinav.moviebooking.booking.cache.BookingCache;
import com.abhinav.moviebooking.booking.domain.Booking;
import com.abhinav.moviebooking.booking.domain.BookingStatus;
import com.abhinav.moviebooking.booking.exception.BookingNotFoundException;
import com.abhinav.moviebooking.booking.persistence.adapter.BookingPersistenceAdapter;
import com.abhinav.moviebooking.booking.seat.service.SeatService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Component;

@Component
public class BookingCancellationService {

    private final BookingPersistenceAdapter bookingPersistenceAdapter;
    private final SeatService seatService;
    private final BookingCache bookingCache;

    public BookingCancellationService(BookingPersistenceAdapter bookingPersistenceAdapter, SeatService seatService, BookingCache bookingCache) {
        this.bookingPersistenceAdapter = bookingPersistenceAdapter;
        this.seatService = seatService;
        this.bookingCache = bookingCache;
    }

    @Transactional
    public void cancelBooking(Long bookingId, BookingCancellationReason reason) {
        Booking booking = bookingPersistenceAdapter.findDomainById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException(bookingId));

        // -----------------------------
        // Idempotency guard
        // -----------------------------
        if (booking.getBookingStatus().isFinal())
            return; // already cancelled / expired

        // -----------------------------
        // Seat compensation
        // -----------------------------
        if (booking.getBookingExecutionContext() != null && booking.getBookingExecutionContext().getAllocatedSeats() != null) {
            seatService.releaseSeats(booking.getBookingExecutionContext().getShowId(),booking.getBookingExecutionContext().getAllocatedSeats());
        }

        // -----------------------------
        // State transition
        // -----------------------------
        booking.transitionTo(
                reason == BookingCancellationReason.EXPIRED ? BookingStatus.EXPIRED : BookingStatus.CANCELLED
        );

        bookingPersistenceAdapter.save(booking);

        // -----------------------------
        // Cache eviction
        // -----------------------------
        bookingCache.evict(bookingId);

    }
}
