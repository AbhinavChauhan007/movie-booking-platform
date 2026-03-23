package com.abhinav.moviebooking.booking.cancellation;

import com.abhinav.moviebooking.booking.cache.BookingCache;
import com.abhinav.moviebooking.booking.domain.Booking;
import com.abhinav.moviebooking.booking.exception.BookingConcurrencyException;
import com.abhinav.moviebooking.booking.exception.BookingNotFoundException;
import com.abhinav.moviebooking.booking.persistence.adapter.BookingPersistenceAdapter;
import com.abhinav.moviebooking.booking.seat.core.SeatService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional(propagation = Propagation.REQUIRED)
    public void cancelBooking(Long bookingId, BookingCancellationReason reason) {

        try {
            Booking booking = bookingPersistenceAdapter.findDomainByIdWithLock(bookingId)
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
                seatService.releaseSeats(booking.getBookingExecutionContext().getShowId(), booking.getBookingExecutionContext().getAllocatedSeats());
            }

            booking.cancel(reason);

            bookingPersistenceAdapter.save(booking);

            // -----------------------------
            // Cache eviction
            // -----------------------------
            bookingCache.evict(bookingId);

        } catch (BookingConcurrencyException e) {
            // Another thread already finalized it → safe to ignore
        }

    }
}
