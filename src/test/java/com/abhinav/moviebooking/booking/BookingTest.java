package com.abhinav.moviebooking.booking;

import com.abhinav.moviebooking.booking.domain.Booking;
import com.abhinav.moviebooking.booking.domain.BookingStatus;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BookingTest {

    @ParameterizedTest
    @MethodSource("com.abhinav.moviebooking.booking.BookingLifecycleTest#validTransitions")
    void bookingShouldAllowValidTransitions(BookingStatus from, BookingStatus to) {

        Booking booking = new Booking(1L);

        // Booking always starts in CREATED
        if (from != BookingStatus.CREATED) {
            moveBookingToState(booking, from);
        }

        booking.transitionTo(to);

        assertEquals(to, booking.getBookingStatus());
    }

    /**
     * Moves booking through valid intermediate states
     */
    private void moveBookingToState(Booking booking, BookingStatus target) {

        switch (target) {
            case INITIATED:
                booking.transitionTo(BookingStatus.INITIATED);
                break;

            case CONFIRMED:
                booking.transitionTo(BookingStatus.INITIATED);
                booking.transitionTo(BookingStatus.CONFIRMED);
                break;

            case CANCELLED:
                booking.transitionTo(BookingStatus.CANCELLED);
                break;

            case EXPIRED:
                booking.transitionTo(BookingStatus.EXPIRED);
                break;

            default:
                throw new IllegalStateException("Unsupported target state: " + target);
        }
    }
}
