package com.abhinav.moviebooking.booking.workflow.impl;

import com.abhinav.moviebooking.booking.domain.Booking;
import com.abhinav.moviebooking.booking.domain.BookingStatus;
import com.abhinav.moviebooking.booking.seat.strategy.SeatAllocationStrategy;
import com.abhinav.moviebooking.booking.seat.strategy.SeatAllocationStrategyFactory;
import com.abhinav.moviebooking.booking.workflow.BookingExecutionContext;
import com.abhinav.moviebooking.booking.workflow.BookingWorkflow;
import com.abhinav.moviebooking.booking.workflow.guard.BookingIdempotencyGuard;
import com.abhinav.moviebooking.pricing.context.PricingContext;
import com.abhinav.moviebooking.pricing.context.PricingRequest;
import com.abhinav.moviebooking.pricing.strategy.PricingStrategy;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;

@Component
public class StandardBookingWorkflow extends BookingWorkflow {

    private final PricingContext pricingContext;
    private final BookingIdempotencyGuard bookingIdempotencyGuard;

    public StandardBookingWorkflow(SeatAllocationStrategyFactory seatAllocationStrategyFactory, PricingContext pricingContext, BookingIdempotencyGuard bookingIdempotencyGuard) {
        super(seatAllocationStrategyFactory);
        this.pricingContext = pricingContext;
        this.bookingIdempotencyGuard = bookingIdempotencyGuard;
    }

    // ==================================================
    // Workflow Steps (Template Method implementation)
    // ==================================================

    @Override
    protected void validate(Booking booking, BookingExecutionContext context) {

        bookingIdempotencyGuard.checkExecutable(booking);

        if (context.getSeatCount() < 0)
            throw new IllegalArgumentException("Seat must be more than zero");

        if (booking.getBookingStatus().isFinal())
            throw new IllegalArgumentException("Booking is already in final state");

        System.out.println("Validating booking request for bookingId : " + booking.getBookingId());
    }

    @Override
    protected void allocateSeats(Booking booking, BookingExecutionContext context) {
        // delegate to SeatAllocationStrategy
        SeatAllocationStrategy seatAllocationStrategy = seatAllocationStrategyFactory.getStrategy(context.getSeatType());
        seatAllocationStrategy.allocateSeats(context.getShowId(), context.getSeatCount());
        booking.transitionTo(BookingStatus.INITIATED);
        System.out.println("Booking " + booking.getBookingId() + " transitioned to INITIATED");
    }

    @Override
    protected void calculatePrice(Booking booking, BookingExecutionContext context) {
        // delegate to PricingStrategy
        double basePrice = context.getSeatCount() * 200; // temporary pricing value
        PricingRequest pricingRequest = new PricingRequest(basePrice, isWeekend(), false // assume non - premium user for now
        );
        PricingStrategy pricingStrategy = pricingContext.resolve(pricingRequest);

        context.setFinalPrice(pricingStrategy.calculatePrice(pricingRequest));
        System.out.println("Final price for booking " + booking.getBookingId() + " is " + context.getFinalPrice());
    }

    @Override
    protected void initiatePayment(Booking booking, BookingExecutionContext context) {
        // payment gateway
        System.out.println("Initiating payment for booking " + booking.getBookingId() + " with amount " + context.getFinalPrice());
    }

    @Override
    protected void confirmBooking(Booking booking, BookingExecutionContext context) {
        // Domain Transition (rules enforced)
        booking.transitionTo(BookingStatus.CONFIRMED);
        System.out.println("Booking " + booking.getBookingId() + " CONFIRMED");
    }

    // ==================================================
    // Helper methods
    // ==================================================

    private boolean isWeekend() {
        DayOfWeek today = LocalDate.now().getDayOfWeek();
        return today == DayOfWeek.SATURDAY || today == DayOfWeek.SUNDAY;
    }
}

