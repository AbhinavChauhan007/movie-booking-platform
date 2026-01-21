package com.abhinav.moviebooking.booking.payment;

import com.abhinav.moviebooking.booking.cache.BookingCache;
import com.abhinav.moviebooking.booking.domain.Booking;
import com.abhinav.moviebooking.booking.domain.BookingStatus;
import com.abhinav.moviebooking.booking.exception.BookingNotFoundException;
import com.abhinav.moviebooking.booking.persistence.adapter.BookingPersistenceAdapter;
import com.abhinav.moviebooking.booking.persistence.adapter.SeatBookingPersistenceAdapter;
import com.abhinav.moviebooking.booking.seat.service.SeatService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Component;

@Component
public class PaymentConfirmationService {

    private final BookingPersistenceAdapter bookingPersistenceAdapter;
    private final SeatBookingPersistenceAdapter seatBookingPersistenceAdapter;
    private final SeatService seatService;
    private final BookingCache bookingCache;

    public PaymentConfirmationService(
            BookingPersistenceAdapter bookingPersistenceAdapter,
            SeatBookingPersistenceAdapter seatBookingPersistenceAdapter,
            SeatService seatService,
            BookingCache bookingCache) {
        this.bookingPersistenceAdapter = bookingPersistenceAdapter;
        this.seatBookingPersistenceAdapter = seatBookingPersistenceAdapter;
        this.seatService = seatService;
        this.bookingCache = bookingCache;
    }

    /**
     * Finalizes booking after successful payment.
     * Fully idempotent.
     */
    @Transactional
    public void confirmPayment(Long bookingId) {
        Booking booking = bookingPersistenceAdapter.findDomainById(bookingId)
                .orElseThrow(
                        () -> new BookingNotFoundException(bookingId)
                );

        // -----------------------------
        // Idempotency guard
        // -----------------------------
        if (booking.getBookingStatus() == BookingStatus.CONFIRMED) {
            return;
        }

        if (booking.getBookingStatus() != BookingStatus.INITIATED) {
            throw new IllegalStateException(
                    "Cannot confirm payment for booking in state: " + booking.getBookingStatus()
            );
        }

        // -----------------------------
        // Persist seat bookings
        // -----------------------------
        seatBookingPersistenceAdapter.saveSeats(
                bookingId,
                booking.getBookingExecutionContext().getShowId(),
                booking.getBookingExecutionContext().getAllocatedSeats()
        );

        // -----------------------------
        // State transition
        // -----------------------------
        booking.transitionTo(BookingStatus.CONFIRMED);
        bookingPersistenceAdapter.save(booking);

        // -----------------------------
        // Redis cleanup
        // (TTL already exists, but explicit cleanup is safe)
        // -----------------------------
        seatService.releaseSeats(
                booking.getBookingExecutionContext().getShowId(),
                booking.getBookingExecutionContext().getAllocatedSeats()
        );

        // -----------------------------
        // Cache refresh
        // -----------------------------
        bookingCache.put(booking);
    }
}

