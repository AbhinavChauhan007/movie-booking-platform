package com.abhinav.moviebooking.booking.lifecycle;

import com.abhinav.moviebooking.booking.domain.BookingStatus;
import com.abhinav.moviebooking.booking.exception.InvalidBookingStateException;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;

public class BookingLifecycle {

    private BookingLifecycle() {
    }

    private static final Map<BookingStatus, EnumSet<BookingStatus>> ALLOWED_TRANSITION = new EnumMap<>(BookingStatus.class);

    static {
        ALLOWED_TRANSITION.put(
                BookingStatus.CREATED,
                EnumSet.of(BookingStatus.INITIATED, BookingStatus.CANCELLED, BookingStatus.EXPIRED)
        );

        ALLOWED_TRANSITION.put(
                BookingStatus.INITIATED,
                EnumSet.of(BookingStatus.CONFIRMED, BookingStatus.EXPIRED, BookingStatus.CANCELLED)
        );

        ALLOWED_TRANSITION.put(
                BookingStatus.CONFIRMED,
                EnumSet.of(BookingStatus.CANCELLED)
        );
    }

    public static void validTransition(BookingStatus current, BookingStatus next) {

        if (current == null || next == null)
            throw new InvalidBookingStateException(
                    "Booking status can not be null");

        if (current == next) {
            throw new InvalidBookingStateException(
                    "Invalid booking transition from " + current + " to " + next
            );
        }

        if (current.isFinal())
            throw new InvalidBookingStateException(
                    "Cannot transition from final state: " + current);

        if (!ALLOWED_TRANSITION.getOrDefault(current, EnumSet.noneOf(BookingStatus.class)).contains(next)) {
            throw new InvalidBookingStateException(
                    "Invalid booking transition from " + current + " to " + next
            );
        }
    }


}
