package com.abhinav.moviebooking.booking.seat.impl;

import com.abhinav.moviebooking.booking.seat.SeatAllocationStrategy;
import com.abhinav.moviebooking.booking.seat.SeatType;
import org.springframework.stereotype.Component;

@Component
public class FirstAvailableSeatStrategy implements SeatAllocationStrategy {

    @Override
    public void allocateSeats(Long showId, int seatCount) {
        System.out.println("Allocating " + seatCount +
                " FIRST AVAILABLE seats for show " + showId);
    }

    @Override
    public void releaseSeats(Long showId, int seatCount) {
        System.out.println("Releasing " + seatCount +
                " FIRST AVAILABLE seats for show " + showId);
    }

    @Override
    public SeatType getSeatType() {
        return SeatType.FIRST_AVAILABLE;
    }
}
