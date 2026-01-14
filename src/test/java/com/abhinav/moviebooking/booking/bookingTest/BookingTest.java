package com.abhinav.moviebooking.booking.bookingTest;

import com.abhinav.moviebooking.booking.domain.Booking;
import com.abhinav.moviebooking.booking.domain.BookingStatus;
import com.abhinav.moviebooking.booking.workflow.BookingExecutionContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class BookingTest {

    /* -----------------------------------------------------------
     * CONSTRUCTION
     * -----------------------------------------------------------
     */

    @Test
    @DisplayName("New booking should start in CREATED state")
    void booking_should_start_in_created_state() {
        Booking booking = new Booking();
        assertEquals(BookingStatus.CREATED, booking.getBookingStatus());
    }

    @Test
    @DisplayName("Booking constructed with status should preserve state")
    void bookingWithProvidedStatusShouldPreserveState() {
        Booking booking = new Booking(1L, BookingStatus.INITIATED);
        assertEquals(BookingStatus.INITIATED, booking.getBookingStatus());
    }

    /* -----------------------------------------------------------
     * ID ASSIGNMENT
     * -----------------------------------------------------------
     */

    @Test
    @DisplayName("Booking ID should be assignable post-creation")
    void shouldAssignBookingId() {
        Booking booking = new Booking();
        booking.assignId(100L);

        assertEquals(100L, booking.getBookingId());
    }

    /* -----------------------------------------------------------
     * EXECUTION CONTEXT
     * -----------------------------------------------------------
     */

    @Test
    @DisplayName("Booking should attach execution context")
    void shouldAttachExecutionContext() {
        Booking booking = new Booking();
        BookingExecutionContext context = new BookingExecutionContext(1L, 2, null);

        booking.attachExecutionContext(context);

        assertEquals(context, booking.getBookingExecutionContext());
    }

    /* -----------------------------------------------------------
     * STATE TRANSITIONS (DELEGATION)
     * -----------------------------------------------------------
     */

    static Stream<Arguments> validTransitions() {
        return Stream.of(
                Arguments.of(BookingStatus.CREATED, BookingStatus.INITIATED),
                Arguments.of(BookingStatus.CREATED, BookingStatus.CANCELLED),
                Arguments.of(BookingStatus.CREATED, BookingStatus.EXPIRED),

                Arguments.of(BookingStatus.INITIATED, BookingStatus.CONFIRMED),
                Arguments.of(BookingStatus.INITIATED, BookingStatus.CANCELLED),
                Arguments.of(BookingStatus.INITIATED, BookingStatus.EXPIRED),

                Arguments.of(BookingStatus.CONFIRMED, BookingStatus.CANCELLED)
        );
    }

    @ParameterizedTest(name = "{0} → {1}")
    @MethodSource("validTransitions")
    @DisplayName("Booking should allow valid transitions")
    void shouldAllowValidTransitions(BookingStatus from, BookingStatus to) {
        Booking booking = new Booking(1L, from);

        booking.transitionTo(to);

        assertEquals(to, booking.getBookingStatus());
    }

    /* -----------------------------------------------------------
     * INVALID TRANSITIONS
     * -----------------------------------------------------------
     */

    static Stream<Arguments> invalidTransitions() {
        return Stream.of(
                // FINAL STATES
                Arguments.of(BookingStatus.CANCELLED, BookingStatus.CREATED),
                Arguments.of(BookingStatus.CANCELLED, BookingStatus.INITIATED),
                Arguments.of(BookingStatus.CANCELLED, BookingStatus.CONFIRMED),
                Arguments.of(BookingStatus.CANCELLED, BookingStatus.EXPIRED),

                Arguments.of(BookingStatus.EXPIRED, BookingStatus.CREATED),
                Arguments.of(BookingStatus.EXPIRED, BookingStatus.INITIATED),
                Arguments.of(BookingStatus.EXPIRED, BookingStatus.CONFIRMED),
                Arguments.of(BookingStatus.EXPIRED, BookingStatus.CANCELLED),

                // BACKWARD MOVES
                Arguments.of(BookingStatus.INITIATED, BookingStatus.CREATED),
                Arguments.of(BookingStatus.CONFIRMED, BookingStatus.INITIATED)
        );
    }

    @ParameterizedTest(name = "{0} → {1}")
    @MethodSource("invalidTransitions")
    @DisplayName("Booking should reject invalid transitions")
    void shouldRejectInvalidTransitions(BookingStatus from, BookingStatus to) {
        Booking booking = new Booking(1L, from);

        assertThrows(IllegalArgumentException.class, () ->
                booking.transitionTo(to)
        );
    }

    /* -----------------------------------------------------------
     * FINAL STATE BEHAVIOR
     * -----------------------------------------------------------
     */

    @Test
    @DisplayName("Final states should report isFinal = true")
    void finalStatesShouldBeFinal() {
        assertTrue(BookingStatus.CANCELLED.isFinal());
        assertTrue(BookingStatus.EXPIRED.isFinal());
    }

    @Test
    @DisplayName("Non-final states should report isFinal = false")
    void nonFinalStatesShouldNotBeFinal() {
        assertFalse(BookingStatus.CREATED.isFinal());
        assertFalse(BookingStatus.INITIATED.isFinal());
        assertFalse(BookingStatus.CONFIRMED.isFinal());
    }
}
