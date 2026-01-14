package com.abhinav.moviebooking.booking.bookingTest;

import com.abhinav.moviebooking.booking.cache.BookingCache;
import com.abhinav.moviebooking.booking.domain.Booking;
import com.abhinav.moviebooking.booking.exception.BookingNotFoundException;
import com.abhinav.moviebooking.booking.read.BookingReadService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class BookingReadServiceTest {

    private BookingCache bookingCache;

    private BookingReadService bookingReadService;

    @BeforeEach
    void setUp() {
        bookingCache = mock(BookingCache.class);
        bookingReadService = new BookingReadService(bookingCache);
    }

    @Test
    @DisplayName("Should return booking from cache")
    void shouldReturnBookingFromCache() {
        Booking cachedBooking = new Booking();
        cachedBooking.assignId(1L);

        when(bookingCache.get(1L)).thenReturn(Optional.of(cachedBooking));

        Booking result = bookingReadService.getBooking(1L);

        assertEquals(1L, result.getBookingId());
        verify(bookingCache).get(1L);
    }

    @Test
    @DisplayName("Should throw exception if booking not found in cache")
    void shouldThrowIfBookingNotInCache() {
        when(bookingCache.get(2L)).thenReturn(Optional.empty());

        assertThrows(
                BookingNotFoundException.class,
                () -> bookingReadService.getBooking(2L)
        );

        verify(bookingCache).get(2L);
    }

}
