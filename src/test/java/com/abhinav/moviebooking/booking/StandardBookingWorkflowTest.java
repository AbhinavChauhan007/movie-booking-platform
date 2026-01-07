package com.abhinav.moviebooking.booking;


import com.abhinav.moviebooking.booking.domain.Booking;
import com.abhinav.moviebooking.booking.domain.BookingStatus;
import com.abhinav.moviebooking.booking.seat.strategy.SeatAllocationStrategy;
import com.abhinav.moviebooking.booking.seat.strategy.SeatAllocationStrategyFactory;
import com.abhinav.moviebooking.booking.seat.strategy.SeatType;
import com.abhinav.moviebooking.booking.workflow.BookingExecutionContext;
import com.abhinav.moviebooking.booking.workflow.guard.BookingIdempotencyGuard;
import com.abhinav.moviebooking.booking.workflow.impl.StandardBookingWorkflow;
import com.abhinav.moviebooking.pricing.context.PricingContext;
import com.abhinav.moviebooking.pricing.context.PricingRequest;
import com.abhinav.moviebooking.pricing.strategy.PricingStrategy;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class StandardBookingWorkflowTest {

    @Test
    void shouldExecuteHappyPath() {
        SeatAllocationStrategyFactory seatAllocationStrategyFactory = mock(SeatAllocationStrategyFactory.class);
        SeatAllocationStrategy seatAllocationStrategy = mock(SeatAllocationStrategy.class);
        when(seatAllocationStrategyFactory.getStrategy(any())).thenReturn(seatAllocationStrategy);

        PricingContext pricingContext = mock(PricingContext.class);
        PricingStrategy pricingStrategy = mock(PricingStrategy.class);

        when(pricingContext.resolve(any(PricingRequest.class))).thenReturn(pricingStrategy);
        when(pricingStrategy.calculatePrice(any(PricingRequest.class))).thenReturn(500.0);

        BookingIdempotencyGuard bookingIdempotencyGuard = new BookingIdempotencyGuard();
        StandardBookingWorkflow standardBookingWorkflow = new StandardBookingWorkflow(seatAllocationStrategyFactory, pricingContext, bookingIdempotencyGuard);
        Booking booking = new Booking(1L);
        BookingExecutionContext bookingExecutionContext = new BookingExecutionContext(1L, 2, SeatType.BEST_AVAILABLE);

        booking.attachExecutionContext(bookingExecutionContext);
        standardBookingWorkflow.execute(booking, bookingExecutionContext);

        verify(seatAllocationStrategy).allocateSeats(1L, 2);
        assertEquals(BookingStatus.CONFIRMED, booking.getBookingStatus());
        assertEquals(500.0, bookingExecutionContext.getFinalPrice());
    }

    @Test
    void shouldRollbackANdReleaseSeatsOnCancel() {
        SeatAllocationStrategyFactory seatAllocationStrategyFactory = mock(SeatAllocationStrategyFactory.class);
        SeatAllocationStrategy seatAllocationStrategy = mock(SeatAllocationStrategy.class);
        when(seatAllocationStrategyFactory.getStrategy(any())).thenReturn(seatAllocationStrategy);

        StandardBookingWorkflow workflow = new StandardBookingWorkflow(seatAllocationStrategyFactory, mock(PricingContext.class), new BookingIdempotencyGuard());
        Booking booking = new Booking(1L);
        BookingExecutionContext context = new BookingExecutionContext(1L, 2, SeatType.BEST_AVAILABLE);
        booking.transitionTo(BookingStatus.INITIATED);
        booking.attachExecutionContext(context);

        workflow.cancelBooking(booking);

        verify(seatAllocationStrategy).releaseSeats(1L, 2);

        assertEquals(BookingStatus.CANCELLED, booking.getBookingStatus());

    }

    @Test
    void shouldRollbackAndReleaseSeatsOnExpire() {
        SeatAllocationStrategyFactory factory = mock(SeatAllocationStrategyFactory.class);
        SeatAllocationStrategy strategy = mock(SeatAllocationStrategy.class);

        when(factory.getStrategy(any())).thenReturn(strategy);

        StandardBookingWorkflow workflow = new StandardBookingWorkflow(factory, mock(PricingContext.class), new BookingIdempotencyGuard());
        Booking booking = new Booking(1L);
        BookingExecutionContext context = new BookingExecutionContext(1L, 2, SeatType.FIRST_AVAILABLE);
        booking.transitionTo(BookingStatus.INITIATED);
        booking.attachExecutionContext(context);

        workflow.expireBooking(booking);

        verify(strategy).releaseSeats(1L, 2);
        assertEquals(BookingStatus.EXPIRED, booking.getBookingStatus());
    }

}
