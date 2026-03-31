package com.abhinav.moviebooking.booking.workflow;

import com.abhinav.moviebooking.booking.domain.Booking;
import com.abhinav.moviebooking.booking.domain.BookingStatus;
import com.abhinav.moviebooking.booking.exception.InvalidBookingStateException;
import com.abhinav.moviebooking.booking.exception.InvalidSeatCountException;
import com.abhinav.moviebooking.show.exception.ShowValidationException;

public abstract class BookingWorkflow {



    // ==================================================
    // Template Method (Happy Path)
    // ==================================================
    public final void execute(Booking booking, BookingExecutionContext context) throws InvalidBookingStateException, InvalidSeatCountException {
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


    protected abstract void validate(Booking booking, BookingExecutionContext context) throws InvalidBookingStateException, InvalidSeatCountException, ShowValidationException;

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
    // Internal Helper
    // ==================================================

    /**
     * Releases seats only if allocation already happened.
     * Safe, idempotent, and lifecycle-aware.
     */
    protected void releaseSeatsIfAllocated(Booking booking) {
        // seat release must be handled by concrete workflow implementation

    }

}
