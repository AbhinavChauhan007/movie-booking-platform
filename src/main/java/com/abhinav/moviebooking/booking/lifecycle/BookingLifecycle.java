package com.abhinav.moviebooking.booking.lifecycle;

import com.abhinav.moviebooking.booking.domain.BookingStatus;

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
            throw new IllegalArgumentException(
                    "Booking status can not be null");

        if (current == next) {
            throw new IllegalArgumentException(
                    "Invalid booking transition from " + current + " to " + next
            );
        }

        if (current.isFinal())
            throw new IllegalArgumentException(
                    "Cannot transition from final state: " + current);

        if (!ALLOWED_TRANSITION.getOrDefault(current, EnumSet.noneOf(BookingStatus.class)).contains(next)) {
            throw new IllegalArgumentException(
                    "Invalid booking transition from " + current + " to " + next
            );
        }
    }


}
