package com.abhinav.moviebooking.booking;

import com.abhinav.moviebooking.booking.lifecycle.BookingLifecycle;
import com.abhinav.moviebooking.booking.domain.BookingStatus;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class BookingLifecycleTest {

    @ParameterizedTest
    @MethodSource("validTransitions")
    void shouldAllowValidTransition(BookingStatus from, BookingStatus to) {
        assertDoesNotThrow(() ->
                BookingLifecycle.validTransition(
                        from,
                        to));
    }

    static Stream<Arguments> validTransitions() {
        return Stream.of(
                // CREATED
                Arguments.of(BookingStatus.CREATED, BookingStatus.INITIATED),
                Arguments.of(BookingStatus.CREATED, BookingStatus.CANCELLED),
                Arguments.of(BookingStatus.CREATED, BookingStatus.EXPIRED),

                //INITIATED
                Arguments.of(BookingStatus.INITIATED, BookingStatus.CONFIRMED),
                Arguments.of(BookingStatus.INITIATED, BookingStatus.CANCELLED),
                Arguments.of(BookingStatus.INITIATED, BookingStatus.EXPIRED),

                // CONFIRMED
                Arguments.of(BookingStatus.CONFIRMED, BookingStatus.CANCELLED)
        );
    }

    @ParameterizedTest
    @MethodSource("invalidFinalTransitions")
    void shouldRejectTransitionFromFinalState(BookingStatus from, BookingStatus to) {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                BookingLifecycle.validTransition(
                        from,
                        to));

        assertTrue(ex.getMessage().contains("final state"));
    }

    static Stream<Arguments> invalidFinalTransitions() {
        return Stream.of(
                Arguments.of(BookingStatus.EXPIRED, BookingStatus.CREATED),
                Arguments.of(BookingStatus.EXPIRED, BookingStatus.INITIATED),
                Arguments.of(BookingStatus.EXPIRED, BookingStatus.CONFIRMED),
                Arguments.of(BookingStatus.EXPIRED, BookingStatus.CANCELLED),
                Arguments.of(BookingStatus.CANCELLED, BookingStatus.CREATED),
                Arguments.of(BookingStatus.CANCELLED, BookingStatus.INITIATED),
                Arguments.of(BookingStatus.CANCELLED, BookingStatus.CONFIRMED),
                Arguments.of(BookingStatus.CANCELLED, BookingStatus.EXPIRED)
        );
    }



    @ParameterizedTest
    @MethodSource("invalidTransitions")
    void shouldRejectInvalidTransition(BookingStatus from, BookingStatus to) {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->BookingLifecycle.validTransition(
                from,
                to
        ));
        assertTrue(ex.getMessage().contains("Invalid booking transition"));
    }

    static Stream<Arguments> invalidTransitions() {
        return Stream.of(
                Arguments.of(BookingStatus.CREATED, BookingStatus.CONFIRMED),
                Arguments.of(BookingStatus.INITIATED, BookingStatus.CREATED),
                Arguments.of(BookingStatus.CONFIRMED, BookingStatus.CREATED),
                Arguments.of(BookingStatus.CONFIRMED, BookingStatus.INITIATED),
                Arguments.of(BookingStatus.CONFIRMED, BookingStatus.EXPIRED)
        );
    }
}
