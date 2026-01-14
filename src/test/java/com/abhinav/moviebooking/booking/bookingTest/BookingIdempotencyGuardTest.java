package com.abhinav.moviebooking.booking.bookingTest;

import com.abhinav.moviebooking.booking.domain.Booking;
import com.abhinav.moviebooking.booking.domain.BookingStatus;
import com.abhinav.moviebooking.booking.workflow.guard.BookingIdempotencyGuard;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class BookingIdempotencyGuardTest {

    private BookingIdempotencyGuard guard;

    @BeforeEach
    public void setUp() {
        guard = new BookingIdempotencyGuard();
    }

    @Test
    void shouldAllowExecutionForNonFinalBooking() {
        Booking booking = new Booking();

        // should not throw exception
        assertDoesNotThrow(() -> guard.checkExecutable(booking));

        booking.transitionTo(BookingStatus.INITIATED);
        assertDoesNotThrow(() -> guard.checkExecutable(booking));
    }

    @Test
    void shouldAllowExecutionForConfirmedBooking() {
        Booking booking = new Booking();
        booking.transitionTo(BookingStatus.INITIATED);
        booking.transitionTo(BookingStatus.CONFIRMED);

        assertDoesNotThrow(() -> guard.checkExecutable(booking));
    }

    @Test
    void shouldThrowIfBookingCancelled() {
        Booking booking = new Booking();
        booking.transitionTo(BookingStatus.CANCELLED);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> guard.checkExecutable(booking)
        );

        assert(ex.getMessage().contains("already cancelled"));
    }

    @Test
    void shouldThrowIfBookingExpired() {
        Booking booking = new Booking();
        booking.transitionTo(BookingStatus.EXPIRED);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> guard.checkExecutable(booking)
        );

        assert(ex.getMessage().contains("already expired"));
    }
}
