package com.abhinav.moviebooking.booking.bookingTest;

import com.abhinav.moviebooking.booking.cache.BookingCache;
import com.abhinav.moviebooking.booking.domain.Booking;
import com.abhinav.moviebooking.booking.domain.BookingStatus;
import com.abhinav.moviebooking.booking.exception.BookingNotFoundException;
import com.abhinav.moviebooking.booking.facade.BookingFacade;
import com.abhinav.moviebooking.booking.persistence.adapter.BookingPersistenceAdapter;
import com.abhinav.moviebooking.booking.persistence.entity.BookingIdempotencyEntity;
import com.abhinav.moviebooking.booking.persistence.repository.BookingIdempotencyRepository;
import com.abhinav.moviebooking.booking.read.BookingReadService;
import com.abhinav.moviebooking.booking.seat.SeatType;
import com.abhinav.moviebooking.booking.workflow.impl.StandardBookingWorkflow;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;

import java.util.Optional;

class BookingFacadeTest {

    private BookingFacade bookingFacade;
    private StandardBookingWorkflow workflow;
    private BookingPersistenceAdapter bookingPersistenceAdapter;
    private BookingReadService bookingReadService;
    private BookingCache bookingCache;
    private BookingIdempotencyRepository bookingIdempotencyRepository;


    @BeforeEach
    void setUp() {
        workflow = mock(StandardBookingWorkflow.class);
        bookingPersistenceAdapter = mock(BookingPersistenceAdapter.class);
        bookingReadService = mock(BookingReadService.class);
        bookingCache = mock(BookingCache.class);
        bookingIdempotencyRepository = mock(BookingIdempotencyRepository.class);
        bookingFacade = new BookingFacade(workflow, bookingPersistenceAdapter, bookingReadService, bookingCache, bookingIdempotencyRepository);
    }

    @Test
    void shouldInitiateBookingSuccessfully() {
        Booking booking = new Booking();
        booking.assignId(1L);

        when(bookingIdempotencyRepository.findById("key-1"))
                .thenReturn(Optional.empty());

        when(bookingPersistenceAdapter.save(any(Booking.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        Booking result = bookingFacade.initiateBooking(10L, 2, SeatType.BEST_AVAILABLE, "key-1");

        // then
        assertNotNull(result);
        verify(workflow).execute(any(), any());
        verify(bookingPersistenceAdapter).save(any(Booking.class));
        verify(bookingCache).put(any(Booking.class));
        verify(bookingIdempotencyRepository).save(any(BookingIdempotencyEntity.class));

    }

    @Test
    void shouldReturnExistingBookingForSameIdempotency() {
        // given
        Booking existingBooking = new Booking();
        existingBooking.assignId(99L);

        when(bookingIdempotencyRepository.findById("dup-key"))
                .thenReturn(Optional.of(new BookingIdempotencyEntity("dup-key", 99L)));

        when(bookingReadService.getBooking(99L))
                .thenReturn(existingBooking);

        // when
        Booking result = bookingFacade.initiateBooking(1L, 1, SeatType.BEST_AVAILABLE, "dup-key");

        // then
        assertEquals(99L, result.getBookingId());
        verify(workflow, never()).execute(any(), any());
        verify(bookingPersistenceAdapter, never()).save(any());
    }

    @Test
    void shouldCancelBooking() {
        Booking booking = new Booking();
        booking.assignId(5L);
        booking.transitionTo(BookingStatus.INITIATED);

        when(bookingReadService.getBooking(5L)).thenReturn(booking);
        when(bookingPersistenceAdapter.save(any())).thenReturn(booking);

        doAnswer(invocation -> {
            Booking b = invocation.getArgument(0);
            b.transitionTo(BookingStatus.CANCELLED);
            return null;
        }).when(workflow).cancelBooking(any());

        Booking result = bookingFacade.cancelBooking(5L);

        assertEquals(BookingStatus.CANCELLED, result.getBookingStatus());
        verify(workflow).cancelBooking(booking);
        verify(bookingCache).put(booking);
    }


    @Test
    void shouldExpireBooking() {
        // given
        Booking booking = new Booking();
        booking.assignId(6L);
        booking.transitionTo(BookingStatus.INITIATED);

        when(bookingReadService.getBooking(6L)).thenReturn(booking);
        when(bookingPersistenceAdapter.save(any())).thenReturn(booking);

        // when
        doAnswer(invocation -> {
            Booking b = invocation.getArgument(0);
            b.transitionTo(BookingStatus.EXPIRED);
            return null;
        }).when(workflow).expireBooking(any());
        Booking result = bookingFacade.expireBooking(6L);

        // then
        assertEquals(BookingStatus.EXPIRED, result.getBookingStatus());
        verify(workflow).expireBooking(booking);
        verify(bookingCache).put(booking);
    }

    @Test
    void shouldThrowIfIdempotencyKeyExistsButBookingMissing() {
        when(bookingIdempotencyRepository.findById("key"))
                .thenReturn(Optional.of(new BookingIdempotencyEntity("key", 99L)));

        when(bookingReadService.getBooking(99L))
                .thenThrow(new BookingNotFoundException(99L));

        assertThrows(
                BookingNotFoundException.class,
                () -> bookingFacade.initiateBooking(1L, 1, SeatType.BEST_AVAILABLE, "key")
        );
    }

    @Test
    void shouldFailCancelIfAlreadyCancelled() {
        Booking booking = new Booking();
        booking.assignId(1L);
        booking.transitionTo(BookingStatus.CANCELLED);

        when(bookingReadService.getBooking(1L)).thenReturn(booking);

        doThrow(new IllegalStateException("Already cancelled"))
                .when(workflow).cancelBooking(booking);

        assertThrows(
                IllegalStateException.class,
                () -> bookingFacade.cancelBooking(1L)
        );
    }


}
