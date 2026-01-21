package com.abhinav.moviebooking.booking.workflow;

import com.abhinav.moviebooking.booking.domain.Booking;
import com.abhinav.moviebooking.booking.domain.BookingStatus;
import com.abhinav.moviebooking.booking.seat.service.SeatService;

public abstract class BookingWorkflow {

    protected final SeatService seatService;

    protected BookingWorkflow(SeatService seatService) {
        this.seatService = seatService;
    }

    // ==================================================
    // Template Method (Happy Path)
    // ==================================================
    public final void execute(Booking booking, BookingExecutionContext context) {
        try {
            validate(booking, context);
            allocateSeats(booking, context);
            calculatePrice(booking, context);
            initiatePayment(booking, context);
            confirmBooking(booking, context);
            notifyUser(booking, context);
        } catch (Exception e) {
            compensate(booking);
            throw e;
        }
    }


    protected abstract void validate(Booking booking, BookingExecutionContext context);

    protected abstract void allocateSeats(Booking booking, BookingExecutionContext context);

    protected abstract void calculatePrice(Booking booking, BookingExecutionContext context);

    protected abstract void initiatePayment(Booking booking, BookingExecutionContext context);

    protected abstract void confirmBooking(Booking booking, BookingExecutionContext context);

    protected void notifyUser(Booking booking, BookingExecutionContext context) {
        // default behavior

    }

    protected void compensate(Booking booking) {
        releaseSeatsIfAllocated(booking);
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

        if (context == null || context.getAllocatedSeats() == null || context.getAllocatedSeats().isEmpty())  // booking never reached seat allocation
            return;

        seatService.releaseSeats(
                context.getShowId(),
                context.getAllocatedSeats()
        );

    }

}
