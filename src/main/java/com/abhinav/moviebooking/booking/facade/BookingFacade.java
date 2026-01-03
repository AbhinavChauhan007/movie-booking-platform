package com.abhinav.moviebooking.booking.facade;

import com.abhinav.moviebooking.booking.seat.strategy.SeatType;
import com.abhinav.moviebooking.booking.workflow.impl.StandardBookingWorkflow;
import org.springframework.stereotype.Component;

@Component
public class BookingFacade {

    private final StandardBookingWorkflow standardBookingWorkflow;

    public BookingFacade(StandardBookingWorkflow standardBookingWorkflow) {
        this.standardBookingWorkflow = standardBookingWorkflow;
    }

    public void initiateBooking(Long bookingId, Long showId, int seatCount, SeatType seatType) {

        // initialize workflow
        standardBookingWorkflow.init(seatType, seatCount, showId, bookingId);

        // execute
        standardBookingWorkflow.execute();
    }

    public void confirmBooking(Long bookingId) {
        standardBookingWorkflow.confirmOnly(bookingId);
    }

    public void allocateSeat(Long showId, int seatCount, SeatType seatType) {
        standardBookingWorkflow.allocateSeatsOnly(showId, seatCount, seatType);
    }
}
