package com.abhinav.moviebooking.booking.workflow.test;

import com.abhinav.moviebooking.booking.seat.strategy.SeatType;
import com.abhinav.moviebooking.booking.workflow.impl.StandardBookingWorkflow;
import org.springframework.beans.factory.annotation.Autowired;

@SpringBootTest
public class StandardBookingWorkflowTest {

    @Autowired
    private StandardBookingWorkflow standardBookingWorkflow;

    @Test
    public void testStandardWorkflowExecution() {

        // 1. Initialize
        standardBookingWorkflow.init(
                SeatType.FIRST_AVAILABLE,
                2, // seatCount
                1001L, // showId
                9002L // bookingId
        );

        // 2. Execute workflow
        standardBookingWorkflow.execute();
    }

}
