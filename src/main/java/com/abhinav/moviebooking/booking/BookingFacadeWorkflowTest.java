package com.abhinav.moviebooking.booking;

import com.abhinav.moviebooking.booking.facade.BookingFacade;
import com.abhinav.moviebooking.booking.seat.strategy.SeatType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class BookingFacadeWorkflowTest {

    @Autowired
    private BookingFacade bookingFacade;

    @Test
    void testFacadeInitiateAndConfirmBooking(){
        // Initiate booking via facade
        bookingFacade.initiateBooking(
                101L,
                501L,
                2,
                SeatType.BEST_AVAILABLE
        );

        bookingFacade.confirmBooking(101L);
    }

}
