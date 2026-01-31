package com.abhinav.moviebooking.booking.payment;

import com.abhinav.moviebooking.booking.cache.BookingCache;
import com.abhinav.moviebooking.booking.domain.Booking;
import com.abhinav.moviebooking.booking.domain.BookingStatus;
import com.abhinav.moviebooking.booking.exception.BookingNotFoundException;
import com.abhinav.moviebooking.booking.persistence.adapter.BookingPersistenceAdapter;
import com.abhinav.moviebooking.booking.persistence.adapter.SeatBookingPersistenceAdapter;
import com.abhinav.moviebooking.booking.seat.core.SeatService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Component;

@Component
public class PaymentConfirmationService {

    private final SeatBookingPersistenceAdapter seatBookingPersistenceAdapter;
    private final BookingCache bookingCache;

    public PaymentConfirmationService(
            SeatBookingPersistenceAdapter seatBookingPersistenceAdapter,
            BookingCache bookingCache) {
        this.seatBookingPersistenceAdapter = seatBookingPersistenceAdapter;
        this.bookingCache = bookingCache;
    }

    /**
     * Finalizes booking using the in-memory object.
     */
    @Transactional
    public void confirmPayment(Booking booking) {
        // 1. Idempotency guard using the object's current state
        if (booking.getBookingStatus() == BookingStatus.CONFIRMED) {
            return;
        }

        // Now this check will PASS because the workflow updated the memory state to INITIATED
        if (booking.getBookingStatus() != BookingStatus.INITIATED) {
            throw new IllegalStateException(
                    "Cannot confirm payment for booking in state: " + booking.getBookingStatus()
            );
        }

        // 2. Persist seat bookings (SQL join table)
        seatBookingPersistenceAdapter.saveSeats(
                booking.getBookingId(),
                booking.getBookingExecutionContext().getShowId(),
                booking.getBookingExecutionContext().getAllocatedSeats()
        );

        // 3. State transition in memory
        booking.transitionTo(BookingStatus.CONFIRMED);

        // NOTE: We do NOT call bookingPersistenceAdapter.save(booking) here.
        // The BookingFacade will handle the final save for the entire transaction.

        // 4. Update Cache (Optional here, or move to Facade)
        bookingCache.put(booking);
    }
}