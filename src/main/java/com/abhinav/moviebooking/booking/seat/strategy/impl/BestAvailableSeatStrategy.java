package com.abhinav.moviebooking.booking.seat.strategy.impl;

import com.abhinav.moviebooking.booking.seat.strategy.SeatAllocationStrategy;
import com.abhinav.moviebooking.booking.seat.strategy.SeatType;
import org.springframework.stereotype.Component;

@Component
public class BestAvailableSeatStrategy implements SeatAllocationStrategy {

    @Override
    public void allocateSeats(Long showId, int seatCount) {
        System.out.println("Allocating " + seatCount + " best available seats for show " + showId);
        // Logic to pick the best available seats
        // This is placeholder for learning purposes
    }

    @Override
    public SeatType getSeatType() {
        return SeatType.BEST_AVAILABLE;
    }

}
