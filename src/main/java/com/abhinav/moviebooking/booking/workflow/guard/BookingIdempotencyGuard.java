package com.abhinav.moviebooking.booking.workflow.guard;

import com.abhinav.moviebooking.booking.domain.Booking;
import com.abhinav.moviebooking.booking.domain.BookingStatus;
import org.springframework.stereotype.Component;

@Component
public class BookingIdempotencyGuard {

    /**
     * Ensures workflow execution is safe and idempotent.
     * Throws early if booking is already in a terminal or processed state.
     */
    public void checkExecutable(Booking booking){

        BookingStatus bookingStatus = booking.getBookingStatus();

        if(bookingStatus == BookingStatus.CONFIRMED){
            throw new IllegalArgumentException("Booking " + booking.getBookingId() + " is already CONFIRMED");
        }

        if(bookingStatus == BookingStatus.CANCELLED){
            throw new IllegalArgumentException("Booking " + booking.getBookingId() + " is already cancelled");
        }

        if(bookingStatus == BookingStatus.EXPIRED){
            throw new IllegalArgumentException("Booking " + booking.getBookingId() + " is already expired");
        }
    }

}
