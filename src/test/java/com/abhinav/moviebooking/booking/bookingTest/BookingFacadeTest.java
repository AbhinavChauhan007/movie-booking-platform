package com.abhinav.moviebooking.booking.bookingTest;

import com.abhinav.moviebooking.booking.cache.BookingCache;
import com.abhinav.moviebooking.booking.cancellation.BookingCancellationReason;
import com.abhinav.moviebooking.booking.cancellation.BookingCancellationService;
import com.abhinav.moviebooking.booking.domain.Booking;
import com.abhinav.moviebooking.booking.domain.BookingStatus;
import com.abhinav.moviebooking.booking.facade.BookingFacade;
import com.abhinav.moviebooking.booking.persistence.adapter.BookingPersistenceAdapter;
import com.abhinav.moviebooking.booking.persistence.entity.BookingIdempotencyEntity;
import com.abhinav.moviebooking.booking.persistence.repository.BookingIdempotencyRepository;
import com.abhinav.moviebooking.booking.read.BookingReadService;
import com.abhinav.moviebooking.booking.seat.strategy.SeatType;
import com.abhinav.moviebooking.booking.workflow.impl.StandardBookingWorkflow;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BookingFacadeTest {

    private static final Long TEST_USER_ID = 456L;

    private BookingFacade bookingFacade;
    private StandardBookingWorkflow workflow;
    private BookingPersistenceAdapter persistenceAdapter;
    private BookingReadService readService;
    private BookingCache bookingCache;
    private BookingIdempotencyRepository idempotencyRepository;
    private BookingCancellationService bookingCancellationService;

    @BeforeEach
    void setUp() {
        workflow = mock(StandardBookingWorkflow.class);
        persistenceAdapter = mock(BookingPersistenceAdapter.class);
        readService = mock(BookingReadService.class);
        bookingCache = mock(BookingCache.class);
        idempotencyRepository = mock(BookingIdempotencyRepository.class);
        bookingCancellationService = mock(BookingCancellationService.class);

        bookingFacade = new BookingFacade(
                workflow,
                persistenceAdapter,
                readService,
                bookingCache,
                idempotencyRepository,
                bookingCancellationService
        );
    }

    @Test
    void initiateBooking_newBooking_success() {
        String idempotencyKey = "key-123";

        Booking persistedBooking = Booking.newBooking(TEST_USER_ID);
        persistedBooking.assignId(100L);

        when(idempotencyRepository.findById(idempotencyKey))
                .thenReturn(Optional.empty());

        when(persistenceAdapter.save(any()))
                .thenReturn(persistedBooking);

        Booking result = bookingFacade.initiateBooking(
                TEST_USER_ID, 1L, 2, SeatType.BEST_AVAILABLE, idempotencyKey
        );

        assertEquals(100L, result.getBookingId());

        verify(idempotencyRepository).save(any(BookingIdempotencyEntity.class));
        verify(workflow).execute(eq(persistedBooking), any());
        verify(bookingCache).put(persistedBooking);
    }

    @Test
    void initiateBooking_existingBooking_returnsExisting() {
        String idempotencyKey = "key-123";

        Booking existingBooking = Booking.newBooking(TEST_USER_ID);
        existingBooking.assignId(42L);

        when(idempotencyRepository.findById(idempotencyKey))
                .thenReturn(Optional.of(
                        new BookingIdempotencyEntity(idempotencyKey, 42L)
                ));

        when(readService.getBooking(42L))
                .thenReturn(existingBooking);

        Booking result = bookingFacade.initiateBooking(
                TEST_USER_ID, 1L, 2, SeatType.FIRST_AVAILABLE, idempotencyKey
        );

        assertEquals(42L, result.getBookingId());
        verify(workflow, never()).execute(any(), any());
        verify(persistenceAdapter, never()).save(any());
    }

    @Test
    void initiateBooking_raceCondition_returnsExisting() {
        String idempotencyKey = "key-123";

        Booking booking = Booking.newBooking(TEST_USER_ID);
        booking.assignId(55L);

        when(idempotencyRepository.findById(idempotencyKey))
                .thenReturn(Optional.empty());

        when(persistenceAdapter.save(any()))
                .thenReturn(booking);

        when(idempotencyRepository.save(any()))
                .thenThrow(DataIntegrityViolationException.class);

        when(idempotencyRepository.findById(idempotencyKey))
                .thenReturn(Optional.of(
                        new BookingIdempotencyEntity(idempotencyKey, 55L)
                ));

        when(readService.getBooking(55L))
                .thenReturn(booking);

        Booking result = bookingFacade.initiateBooking(
                TEST_USER_ID, 1L, 1, SeatType.FIRST_AVAILABLE, idempotencyKey
        );

        assertEquals(55L, result.getBookingId());
    }

    @Test
    void cancelBooking_delegatesToCancellationService() {
        Booking booking = Booking.newBooking(TEST_USER_ID);
        booking.assignId(200L);

        when(readService.getBooking(200L)).thenReturn(booking);

        Booking result = bookingFacade.cancelBooking(200L);

        verify(bookingCancellationService)
                .cancelBooking(200L, BookingCancellationReason.USER_CANCELLED);

        assertEquals(booking, result);
    }

    @Test
    void expireBooking_delegatesToCancellationService() {
        Booking booking = Booking.newBooking(TEST_USER_ID);
        booking.assignId(300L);

        when(readService.getBooking(300L)).thenReturn(booking);

        Booking result = bookingFacade.expireBooking(300L);

        verify(bookingCancellationService)
                .cancelBooking(300L, BookingCancellationReason.EXPIRED);

        assertEquals(booking, result);
    }

    @Test
    void getStatus_returnsCorrectStatus() {
        Booking booking = mock(Booking.class);

        when(booking.getBookingStatus())
                .thenReturn(BookingStatus.CONFIRMED);

        when(readService.getBooking(400L))
                .thenReturn(booking);

        assertEquals(
                BookingStatus.CONFIRMED,
                bookingFacade.getStatus(400L)
        );
    }
}
