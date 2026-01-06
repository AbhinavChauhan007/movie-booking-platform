package com.abhinav.moviebooking;

import com.abhinav.moviebooking.booking.lifecycle.BookingLifecycle;
import com.abhinav.moviebooking.booking.domain.BookingStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class BookingLifecycleTest {

    @Test
    void shouldAllowValidTransition() {
        assertDoesNotThrow(() ->
                BookingLifecycle.validTransition(
                        BookingStatus.INITIATED,
                        BookingStatus.CONFIRMED));
    }

    @Test
    void shouldRejectInvalidTransition() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                BookingLifecycle.validTransition(
                        BookingStatus.CONFIRMED,
                        BookingStatus.CANCELLED));

        assertTrue(ex.getMessage().contains("final state"));
    }
}
