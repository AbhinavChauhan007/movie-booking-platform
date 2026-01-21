package com.abhinav.moviebooking.booking.bookingTest;

import com.abhinav.moviebooking.booking.cancellation.BookingCancellationReason;
import com.abhinav.moviebooking.booking.cancellation.BookingCancellationService;
import com.abhinav.moviebooking.booking.domain.Booking;
import com.abhinav.moviebooking.booking.domain.BookingStatus;
import com.abhinav.moviebooking.booking.payment.PaymentConfirmationService;
import com.abhinav.moviebooking.booking.seat.service.SeatService;
import com.abhinav.moviebooking.booking.seat.strategy.SeatType;
import com.abhinav.moviebooking.booking.workflow.BookingExecutionContext;
import com.abhinav.moviebooking.booking.workflow.guard.BookingIdempotencyGuard;
import com.abhinav.moviebooking.booking.workflow.impl.StandardBookingWorkflow;
import com.abhinav.moviebooking.pricing.context.PricingContext;
import com.abhinav.moviebooking.pricing.context.PricingRequest;
import com.abhinav.moviebooking.pricing.strategy.PricingStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class StandardBookingWorkflowTest {

    private SeatService seatService;
    private PricingContext pricingContext;
    private PricingStrategy pricingStrategy;
    private BookingIdempotencyGuard bookingIdempotencyGuard;
    private BookingCancellationService bookingCancellationService;
    private PaymentConfirmationService paymentConfirmationService;

    private StandardBookingWorkflow workflow;

    @BeforeEach
    void setup() {
        seatService = mock(SeatService.class);
        pricingContext = mock(PricingContext.class);
        pricingStrategy = mock(PricingStrategy.class);
        bookingIdempotencyGuard = mock(BookingIdempotencyGuard.class);
        bookingCancellationService = mock(BookingCancellationService.class);
        paymentConfirmationService = mock(PaymentConfirmationService.class);

        when(seatService.allocateSeats(any(), anyInt(), any()))
                .thenReturn(List.of("A1", "A2"));

        when(pricingContext.resolve(any(PricingRequest.class)))
                .thenReturn(pricingStrategy);

        when(pricingStrategy.calculatePrice(any()))
                .thenReturn(500.0);

        workflow = new StandardBookingWorkflow(
                seatService,
                pricingContext,
                bookingIdempotencyGuard,
                bookingCancellationService,
                paymentConfirmationService
        );
    }

    /* --------------------------------------------------
     * HAPPY PATH
     * -------------------------------------------------- */

    @Test
    @DisplayName("Booking should complete successfully")
    void shouldExecuteWorkflowSuccessfully() {
        Booking booking = Booking.newBooking();
        BookingExecutionContext ctx =
                new BookingExecutionContext(10L, 2, SeatType.BEST_AVAILABLE);

        booking.attachExecutionContext(ctx);

        workflow.execute(booking, ctx);

        verify(bookingIdempotencyGuard).checkExecutable(booking);
        verify(seatService).allocateSeats(10L, 2, booking.getBookingId());
        verify(pricingContext).resolve(any());
        verify(paymentConfirmationService).confirmPayment(booking.getBookingId());

        assertEquals(500.0, ctx.getFinalPrice());
        assertEquals(BookingStatus.CONFIRMED, booking.getBookingStatus());
    }

    /* --------------------------------------------------
     * VALIDATION FAILURE
     * -------------------------------------------------- */

    @Test
    void shouldFailIfSeatCountInvalid() {
        Booking booking = Booking.newBooking();
        BookingExecutionContext ctx =
                new BookingExecutionContext(10L, -1, SeatType.BEST_AVAILABLE);

        booking.attachExecutionContext(ctx);

        assertThrows(
                IllegalArgumentException.class,
                () -> workflow.execute(booking, ctx)
        );

        verifyNoInteractions(seatService, pricingContext);
        assertEquals(BookingStatus.CREATED, booking.getBookingStatus());
    }

    /* --------------------------------------------------
     * SEAT ALLOCATION FAILURE
     * -------------------------------------------------- */

    @Test
    void shouldCompensateIfSeatAllocationFails() {
        Booking booking = Booking.newBooking();
        BookingExecutionContext ctx =
                new BookingExecutionContext(5L, 2, SeatType.BEST_AVAILABLE);

        booking.attachExecutionContext(ctx);

        when(seatService.allocateSeats(any(), anyInt(), any()))
                .thenThrow(new RuntimeException("No seats"));

        assertThrows(
                RuntimeException.class,
                () -> workflow.execute(booking, ctx)
        );

        verify(bookingCancellationService)
                .cancelBooking(booking.getBookingId(), BookingCancellationReason.SYSTEM_ERROR);
    }

    /* --------------------------------------------------
     * IDEMPOTENCY FAILURE
     * -------------------------------------------------- */

    @Test
    void shouldStopIfIdempotencyGuardFails() {
        Booking booking = Booking.newBooking();
        BookingExecutionContext ctx =
                new BookingExecutionContext(1L, 1, SeatType.BEST_AVAILABLE);

        booking.attachExecutionContext(ctx);

        doThrow(new IllegalStateException("Duplicate"))
                .when(bookingIdempotencyGuard)
                .checkExecutable(booking);

        assertThrows(
                IllegalStateException.class,
                () -> workflow.execute(booking, ctx)
        );

        verifyNoInteractions(seatService, pricingContext);
    }
}
