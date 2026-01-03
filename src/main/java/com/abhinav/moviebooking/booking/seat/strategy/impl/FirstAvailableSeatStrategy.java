package com.abhinav.moviebooking.booking.seat.strategy.impl;

import com.abhinav.moviebooking.booking.seat.strategy.SeatAllocationStrategy;
import com.abhinav.moviebooking.booking.seat.strategy.SeatType;
import org.springframework.stereotype.Component;

@Component
public class FirstAvailableSeatStrategy implements SeatAllocationStrategy {

    @Override
    public void allocateSeats(Long showId, int seatCount) {
        System.out.println("Allocating " + seatCount + " first available seats for show " + showId);
        // Logic to pick the first available seats
        // This is placeholder for learning purposes
    }

    @Override
    public SeatType getSeatType() {
        return SeatType.FIRST_AVAILABLE;
    }
}
