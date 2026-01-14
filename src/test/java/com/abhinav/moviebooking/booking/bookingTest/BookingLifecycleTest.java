package com.abhinav.moviebooking.booking.bookingTest;

import com.abhinav.moviebooking.booking.lifecycle.BookingLifecycle;
import com.abhinav.moviebooking.booking.domain.BookingStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class BookingLifecycleTest {

    /* -----------------------------------------------------------
     * VALID TRANSITIONS
     * -----------------------------------------------------------
     */

    @ParameterizedTest(name = "VALID: {0} → {1}")
    @MethodSource("validTransitions")
    @DisplayName("Should allow valid booking state transitions")
    void shouldAllowValidTransition(BookingStatus from, BookingStatus to) {
        assertDoesNotThrow(() ->
                BookingLifecycle.validTransition(from,to));
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

    /* -----------------------------------------------------------
     * INVALID TRANSITIONS
     * -----------------------------------------------------------
     */

    static Stream<Arguments> invalidTransitions() {
        return allTransitionsExcluding(validTransitions());
    }

    @ParameterizedTest(name = "INVALID: {0} → {1}")
    @MethodSource("invalidTransitions")
    @DisplayName("Should reject invalid booking state transitions")
    void shouldRejectInvalidTransition(BookingStatus from, BookingStatus to) {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->BookingLifecycle.validTransition(
                from,
                to
        ));
        assertTrue(ex.getMessage().contains("Invalid")
                || ex.getMessage().contains("final"));
    }

    /* -----------------------------------------------------------
     * FINAL STATE SAFETY
     * -----------------------------------------------------------
     */

    @Test
    @DisplayName("Final states should not allow any outgoing transitions")
    void finalStatesShouldNotTransition() {
        Set<BookingStatus> finalStates = EnumSet.of(
                BookingStatus.CANCELLED,
                BookingStatus.EXPIRED
        );

        for (BookingStatus from : finalStates) {
            for (BookingStatus to : BookingStatus.values()) {
                assertThrows(IllegalArgumentException.class, () ->
                                BookingLifecycle.validTransition(from, to),
                        "Transition should be blocked: " + from + " → " + to
                );
            }
        }
    }

    private static Stream<Arguments> allTransitionsExcluding(Stream<Arguments> validTransitions) {

        Set<String> validPairs = validTransitions
                .map(arg -> arg.get()[0] + "->" + arg.get()[1])
                .collect(java.util.stream.Collectors.toSet());

        return Stream.of(BookingStatus.values())
                .flatMap(from ->
                        Stream.of(BookingStatus.values())
                                .filter(to ->
                                        !validPairs.contains(from + "->" + to)
                                )
                                .map(to -> Arguments.of(from, to))
                );
    }
}
