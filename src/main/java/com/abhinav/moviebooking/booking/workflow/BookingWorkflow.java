package com.abhinav.moviebooking.booking.workflow;

import com.abhinav.moviebooking.booking.domain.Booking;
import com.abhinav.moviebooking.booking.domain.BookingStatus;
import com.abhinav.moviebooking.booking.seat.strategy.SeatAllocationStrategy;
import com.abhinav.moviebooking.booking.seat.strategy.SeatAllocationStrategyFactory;

public abstract class BookingWorkflow {

    protected final SeatAllocationStrategyFactory seatAllocationStrategyFactory;

    protected BookingWorkflow(SeatAllocationStrategyFactory seatAllocationStrategyFactory) {
        this.seatAllocationStrategyFactory = seatAllocationStrategyFactory;
    }


    // ==================================================
    // Template Method (Happy Path)
    // ==================================================
    public final void execute(Booking booking, BookingExecutionContext context) {
        validate(booking, context);
        allocateSeats(booking, context);
        calculatePrice(booking, context);
        initiatePayment(booking, context);
        confirmBooking(booking, context);
        notifyUser(booking, context);
    }


    protected abstract void validate(Booking booking, BookingExecutionContext context);

    protected abstract void allocateSeats(Booking booking, BookingExecutionContext context);

    protected abstract void calculatePrice(Booking booking, BookingExecutionContext context);

    protected abstract void initiatePayment(Booking booking, BookingExecutionContext context);

    protected abstract void confirmBooking(Booking booking, BookingExecutionContext context);

    protected void notifyUser(Booking booking, BookingExecutionContext context) {
        // default behavior

    }

    // ==================================================
    // Compensation Actions (Rollback Paths)
    // ==================================================

    public void cancelBooking(Booking booking) {
        releaseSeatsIfAllocated(booking);

        booking.transitionTo(BookingStatus.CANCELLED);
        System.out.println("Booking " + booking.getBookingId() + " has been cancelled");
    }

    public void expireBooking(Booking booking) {
        releaseSeatsIfAllocated(booking);

        booking.transitionTo(BookingStatus.EXPIRED);
        System.out.println("Booking " + booking.getBookingId() + " has been expired");
    }

    // ==================================================
    // Internal Helper
    // ==================================================

    /**
     * Releases seats only if allocation already happened.
     * Safe, idempotent, and lifecycle-aware.
     */
    protected void releaseSeatsIfAllocated(Booking booking) {
        BookingExecutionContext context = booking.getBookingExecutionContext();

        if (context == null)  // booking never reached seat allocation
            return;

        SeatAllocationStrategy strategy = seatAllocationStrategyFactory.getStrategy(context.getSeatType());

        strategy.releaseSeats(context.getShowId(), context.getSeatCount());

        System.out.println(
                "Released " + context.getSeatCount() +
                        " seats for booking " + booking.getBookingId()
        );
    }

}
