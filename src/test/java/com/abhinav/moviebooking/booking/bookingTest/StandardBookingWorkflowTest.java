package com.abhinav.moviebooking.booking.bookingTest;


import com.abhinav.moviebooking.booking.domain.Booking;
import com.abhinav.moviebooking.booking.domain.BookingStatus;
import com.abhinav.moviebooking.booking.seat.SeatAllocationStrategy;
import com.abhinav.moviebooking.booking.seat.SeatAllocationStrategyFactory;
import com.abhinav.moviebooking.booking.seat.SeatType;
import com.abhinav.moviebooking.booking.workflow.BookingExecutionContext;
import com.abhinav.moviebooking.booking.workflow.guard.BookingIdempotencyGuard;
import com.abhinav.moviebooking.booking.workflow.impl.StandardBookingWorkflow;
import com.abhinav.moviebooking.pricing.context.PricingContext;
import com.abhinav.moviebooking.pricing.context.PricingRequest;
import com.abhinav.moviebooking.pricing.strategy.PricingStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class StandardBookingWorkflowTest {

    private SeatAllocationStrategyFactory seatAllocationStrategyFactory;
    private SeatAllocationStrategy seatAllocationStrategy;
    private PricingContext pricingContext;
    private PricingStrategy pricingStrategy;
    private BookingIdempotencyGuard bookingIdempotencyGuard;

    private StandardBookingWorkflow standardBookingWorkflow;

    @BeforeEach
    void setup() {
        seatAllocationStrategyFactory = mock(SeatAllocationStrategyFactory.class);
        seatAllocationStrategy = mock(SeatAllocationStrategy.class);
        pricingStrategy = mock(PricingStrategy.class);
        pricingContext = mock(PricingContext.class);
        bookingIdempotencyGuard = mock(BookingIdempotencyGuard.class);


        when(seatAllocationStrategyFactory.getStrategy(any()))
                .thenReturn(seatAllocationStrategy);

        when(pricingContext.resolve(any()))
                .thenReturn(pricingStrategy);

        when(pricingStrategy.calculatePrice(any(PricingRequest.class)))
                .thenReturn(500.0);

        standardBookingWorkflow = new StandardBookingWorkflow(
                seatAllocationStrategyFactory,
                pricingContext,
                bookingIdempotencyGuard
        );
    }

    /* --------------------------------------------------
     * HAPPY PATH
     * -------------------------------------------------- */

    @Test
    @DisplayName("Booking should move CREATED → INITIATED → CONFIRMED")
    void shouldExecuteFullWorkflowSuccessfully() {
        Booking booking = new Booking();
        BookingExecutionContext bookingExecutionContext = new BookingExecutionContext(10L, 2, SeatType.BEST_AVAILABLE);
        booking.attachExecutionContext(bookingExecutionContext);

        standardBookingWorkflow.execute(booking, bookingExecutionContext);

        verify(bookingIdempotencyGuard).checkExecutable(booking);
        verify(seatAllocationStrategy).allocateSeats(10L, 2);
        verify(seatAllocationStrategyFactory).getStrategy(SeatType.BEST_AVAILABLE);

        assertEquals(500.0, bookingExecutionContext.getFinalPrice());
        assertEquals(BookingStatus.CONFIRMED, booking.getBookingStatus());

    }

    /* --------------------------------------------------
     * VALIDATION FAILURE
     * -------------------------------------------------- */

    @Test
    @DisplayName("Workflow should fail fast if validation fails")
    void shouldFailIfSeatCountInvalid() {
        Booking booking = new Booking();
        BookingExecutionContext context = new BookingExecutionContext(10L, -1, SeatType.BEST_AVAILABLE);
        booking.attachExecutionContext(context);

        assertThrows(IllegalArgumentException.class, () -> standardBookingWorkflow.execute(booking, context));
        verifyNoInteractions(pricingContext);
        verifyNoInteractions(seatAllocationStrategy);
        assertEquals(BookingStatus.CREATED, booking.getBookingStatus());
    }

    /* --------------------------------------------------
     * SEAT ALLOCATION FAILURE
     * -------------------------------------------------- */

    @Test
    @DisplayName("Seat allocation failure should stop workflow")
    void shouldStopWorkflowIfSeatAllocationFails() {
        Booking booking = new Booking();
        BookingExecutionContext context = new BookingExecutionContext(5L, 2, SeatType.BEST_AVAILABLE);
        booking.attachExecutionContext(context);

        doThrow(new RuntimeException("No Seats"))
                .when(seatAllocationStrategy)
                .allocateSeats(5L, 2);

        assertThrows(RuntimeException.class,
                () -> standardBookingWorkflow.execute(booking, context));

        verify(seatAllocationStrategy).allocateSeats(5L, 2);
        verifyNoInteractions(pricingContext);

        assertEquals(BookingStatus.CREATED, booking.getBookingStatus());
    }

    /* --------------------------------------------------
     * CANCEL FLOW
     * -------------------------------------------------- */

    @Test
    @DisplayName("Cancel should release seats and mark booking CANCELLED")
    void shouldCancelBookingAndReleaseSeats() {
        Booking booking = new Booking();
        BookingExecutionContext context = new BookingExecutionContext(7L, 3, SeatType.BEST_AVAILABLE);
        booking.attachExecutionContext(context);
        booking.transitionTo(BookingStatus.INITIATED);

        standardBookingWorkflow.cancelBooking(booking);

        verify(seatAllocationStrategy).releaseSeats(7L, 3);

        assertEquals(BookingStatus.CANCELLED, booking.getBookingStatus());
    }

    /* --------------------------------------------------
     * EXPIRE FLOW
     * -------------------------------------------------- */

    @Test
    @DisplayName("Expire should release seats and mark booking EXPIRED")
    void shouldExpireBooking() {
        Booking booking = new Booking();
        BookingExecutionContext context =
                new BookingExecutionContext(8L, 1, SeatType.BEST_AVAILABLE);

        booking.attachExecutionContext(context);
        booking.transitionTo(BookingStatus.INITIATED);

        standardBookingWorkflow.expireBooking(booking);

        verify(seatAllocationStrategy)
                .releaseSeats(8L, 1);

        assertEquals(BookingStatus.EXPIRED, booking.getBookingStatus());
    }

    @Test
    @DisplayName("Workflow should stop if idempotency guard blocks execution")
    void shouldStopIfIdempotencyGuardFails() {
        Booking booking = new Booking();
        BookingExecutionContext ctx =
                new BookingExecutionContext(1L, 1, SeatType.BEST_AVAILABLE);
        booking.attachExecutionContext(ctx);

        doThrow(new IllegalArgumentException("Already processed"))
                .when(bookingIdempotencyGuard)
                .checkExecutable(booking);

        assertThrows(
                IllegalArgumentException.class,
                () -> standardBookingWorkflow.execute(booking, ctx)
        );

        verifyNoInteractions(seatAllocationStrategy);
        verifyNoInteractions(pricingContext);
    }

}
