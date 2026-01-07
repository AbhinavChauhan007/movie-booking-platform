package com.abhinav.moviebooking.booking;

import com.abhinav.moviebooking.booking.domain.Booking;
import com.abhinav.moviebooking.booking.facade.BookingFacade;
import com.abhinav.moviebooking.booking.seat.strategy.SeatType;
import com.abhinav.moviebooking.booking.store.InMemoryBookingStore;
import com.abhinav.moviebooking.booking.workflow.impl.StandardBookingWorkflow;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.mockito.*;

class BookingFacadeTest {

    private BookingFacade bookingFacade;
    private StandardBookingWorkflow workflow;
    private InMemoryBookingStore bookingStore;

    @BeforeEach
    void setUp() {
        workflow = mock(StandardBookingWorkflow.class);
        bookingStore = new InMemoryBookingStore();
        bookingFacade = new BookingFacade(workflow, bookingStore);
    }

    @Test
    void shouldInitiateBookingSuccessfully() {
        Booking booking = bookingFacade.initiateBooking(1L, 10L, 1, SeatType.FIRST_AVAILABLE);

        assertNotNull(booking);
        assertEquals(1L, booking.getBookingId());
        assertNotNull(booking.getBookingExecutionContext());
        verify(workflow).execute(
                eq(booking),
                argThat(ctx ->
                        ctx.getShowId().equals(10L)
                                && ctx.getSeatCount() == 1
                                && ctx.getSeatType().equals(SeatType.FIRST_AVAILABLE)
                ));

    }

    void shouldCancelBooking() {
        bookingFacade.initiateBooking(2L, 20L, 2, SeatType.BEST_AVAILABLE);

        Booking booking = bookingFacade.cancelBooking(2L);

        verify(workflow).cancelBooking(booking);

    }
    @Test
    void shouldExpireBooking() {
        bookingFacade.initiateBooking(3L, 30L, 3, SeatType.FIRST_AVAILABLE);

        Booking booking = bookingFacade.expireBooking(3L);

        verify(workflow).expireBooking(booking);
    }

}
