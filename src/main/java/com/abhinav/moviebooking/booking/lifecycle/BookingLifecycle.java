package com.abhinav.moviebooking.booking.lifecycle;

import com.abhinav.moviebooking.booking.domain.BookingStatus;

import java.util.EnumSet;
import java.util.Map;

import static java.util.Map.entry;

public class BookingLifecycle {

    private BookingLifecycle() {
    }

    private static final Map<BookingStatus, EnumSet<BookingStatus>> ALLOWED_TRANSACTION
            = Map.ofEntries(
            entry(BookingStatus.INITIATED, EnumSet.of(BookingStatus.SEATS_LOCKED, BookingStatus.CANCELLED)),
            entry(BookingStatus.SEATS_LOCKED, EnumSet.of(BookingStatus.PENDING_PAYMENT, BookingStatus.EXPIRED, BookingStatus.CANCELLED)),
            entry(BookingStatus.PENDING_PAYMENT, EnumSet.of(BookingStatus.CONFIRMED, BookingStatus.EXPIRED, BookingStatus.CANCELLED)),
            entry(BookingStatus.CONFIRMED, EnumSet.noneOf(BookingStatus.class)),
            entry(BookingStatus.CANCELLED, EnumSet.noneOf(BookingStatus.class)),
            entry(BookingStatus.EXPIRED, EnumSet.noneOf(BookingStatus.class)));

    public static void validTransition(BookingStatus current, BookingStatus next) {

        if (current == null || next == null)
            throw new IllegalArgumentException(
                    "Booking status can not be null");

        if (current.isFinal())
            throw new IllegalArgumentException(
                    "Cannot transition from final state: " + current);

        EnumSet<BookingStatus> allowedStatus = ALLOWED_TRANSACTION.get(current);

        if (allowedStatus == null || !allowedStatus.contains(next))
            throw new IllegalStateException(
                    "Invalid booking status transition: " + current + " -> " + next
            );
    }


}
