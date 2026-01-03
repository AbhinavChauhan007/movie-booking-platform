package com.abhinav.moviebooking.booking.workflow;

public abstract class BookingWorkflow {

    // Template Method
    public final void execute() {
        validate();
        allocateSeats();
        calculatePrice();
        initiatePayment();
        confirmBooking();
        notifyUser();
    }


    protected abstract void validate();

    protected abstract void allocateSeats();

    protected abstract void calculatePrice();

    protected abstract void initiatePayment();

    protected abstract void confirmBooking();

    protected void notifyUser() {
        // default behavior

    }

}
