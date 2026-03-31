package com.abhinav.moviebooking.booking.bookingTest;

import com.abhinav.moviebooking.booking.domain.Booking;
import com.abhinav.moviebooking.booking.domain.BookingStatus;
import com.abhinav.moviebooking.booking.exception.InvalidBookingStateException;
import com.abhinav.moviebooking.booking.workflow.guard.BookingIdempotencyGuard;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class BookingIdempotencyGuardTest {

    private static final Long TEST_USER_ID = 999L;

    private BookingIdempotencyGuard guard;

    @BeforeEach
    public void setUp() {
        guard = new BookingIdempotencyGuard();
    }

    @Test
    void shouldAllowExecutionForNonFinalBooking() {
        Booking booking = Booking.newBooking(TEST_USER_ID);

        // should not throw exception
        assertDoesNotThrow(() -> guard.checkExecutable(booking));

        booking.transitionTo(BookingStatus.INITIATED);
        assertDoesNotThrow(() -> guard.checkExecutable(booking));
    }

    @Test
    void shouldAllowExecutionForConfirmedBooking() {
        Booking booking = Booking.newBooking(TEST_USER_ID);
        booking.transitionTo(BookingStatus.INITIATED);
        booking.transitionTo(BookingStatus.CONFIRMED);

        assertDoesNotThrow(() -> guard.checkExecutable(booking));
    }

    @Test
    void shouldThrowIfBookingCancelled() {
        Booking booking = Booking.newBooking(TEST_USER_ID);
        booking.transitionTo(BookingStatus.CANCELLED);

        InvalidBookingStateException ex = assertThrows(
                InvalidBookingStateException.class,
                () -> guard.checkExecutable(booking)
        );

        assertTrue(ex.getMessage().contains("already cancelled"));
    }

    @Test
    void shouldThrowIfBookingExpired() {
        Booking booking = Booking.newBooking(TEST_USER_ID);
        booking.transitionTo(BookingStatus.EXPIRED);

        InvalidBookingStateException ex = assertThrows(
                InvalidBookingStateException.class,
                () -> guard.checkExecutable(booking)
        );

        assertTrue(ex.getMessage().contains("already expired"));
    }
}
